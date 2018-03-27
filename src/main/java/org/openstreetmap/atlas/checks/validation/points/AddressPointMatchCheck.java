package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.AddressHousenumberTag;
import org.openstreetmap.atlas.tags.AddressStreetTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Tests for {@link AddressPointMatchCheck}
 *
 * @author savannahostrowski
 */

public class AddressPointMatchCheck extends BaseCheck
{
    private static final long serialVersionUID = 1L;
    public static final String NO_STREET_NAME_POINT_INSTRUCTIONS = "This node, {0,number,#}, has "
            + "no street name specified in the address. The street name should likely "
            + "be one of {1}. These names were derived from nearby Points.";
    public static final String NO_STREET_NAME_EDGE_INSTRUCTIONS = "This node, {0,number,#}, has "
            + "no street name specified in the address. The street name should likely "
            + "be one of {1}. These names were derived from nearby Edges.";
    public static final String NO_SUGGESTED_NAMES_INSTRUCTIONS = "This node, {0,number,#}, has "
            + "no street name specified in the address. No suggestions names were found.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            NO_STREET_NAME_POINT_INSTRUCTIONS, NO_STREET_NAME_EDGE_INSTRUCTIONS,
            NO_SUGGESTED_NAMES_INSTRUCTIONS);
    private static final String ADDRESS_STREET_NUMBER_KEY = AddressHousenumberTag.KEY;
    private static final String POINT_STREET_NAME_KEY = AddressStreetTag.KEY;
    private static final String EDGE_STREET_NAME_KEY = NameTag.KEY;
    private static final double BOUNDS_SIZE_DEFAULT = 150.0;

    private final Distance boundsSize;

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public AddressPointMatchCheck(final Configuration configuration)
    {
        super(configuration);
        this.boundsSize = Distance.meters(
                (Double) configurationValue(configuration, "bounds.size", BOUNDS_SIZE_DEFAULT));
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Object is an instance of Point
        return object instanceof Point
                // And has a street number specified
                && object.getTag(ADDRESS_STREET_NUMBER_KEY).isPresent()
                // And if the street name key has a value of null
                && (!object.getTag(POINT_STREET_NAME_KEY).isPresent()
                        // Or if the street name key is not present
                        || !object.getTags().containsKey(POINT_STREET_NAME_KEY));
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Point point = (Point) object;
        // Get a bounding box around the Point of interest
        final Rectangle box = point.getLocation().boxAround(boundsSize);

        // Get all Points inside the bounding box
        final Iterable<Point> interiorPoints = point.getAtlas().pointsWithin(box);
        // Convert the Iterable into a Stream for filtering, remove Points that have null as their
        // street name as they cannot be candidates for a street for the Point of interest
        final Set<Point> points = StreamSupport.stream(interiorPoints.spliterator(), false)
                .filter(interiorPoint -> interiorPoint.getTag(POINT_STREET_NAME_KEY).isPresent())
                .collect(Collectors.toSet());

        // Get all Edges that intersect or are contained within the bounding box
        final Iterable<Edge> interiorEdges = point.getAtlas().edgesIntersecting(box);
        // Convert the Iterable into a Stream for filtering, remove Edge that have null as their
        // street name as they cannot be candidates for a street for the Point of interest
        final Set<Edge> edges = StreamSupport.stream(interiorEdges.spliterator(), false)
                .filter(interiorEdge -> interiorEdge.getTag(EDGE_STREET_NAME_KEY).isPresent())
                .collect(Collectors.toSet());

        final Set<String> streetNameList = new HashSet<>();

        // If there are no Points or Edges in the bounding box
        if (points.isEmpty() && edges.isEmpty())
        {
            // Flag Point with instruction indicating that there are are no suggestions
            return Optional.of(this.createFlag(point,
                    this.getLocalizedInstruction(2, point.getOsmIdentifier())));
        }
        // If there are Points in the bounding box
        else if (!points.isEmpty())
        {
            // Add all interior Point street names to the list of candidate street names
            points.forEach(interiorPoint -> streetNameList
                    .add(interiorPoint.getTags().get(POINT_STREET_NAME_KEY)));
            return Optional.of(this.createFlag(point,
                    this.getLocalizedInstruction(0, point.getOsmIdentifier(), streetNameList)));

        }
        // If there are Edges intersecting or contained by the bounding box
        else
        {
            // Add all interior Edge street names to the list of candidate street names
            edges.forEach(interiorEdge -> streetNameList
                    .add(interiorEdge.getTags().get(EDGE_STREET_NAME_KEY)));
            return Optional.of(this.createFlag(point,
                    this.getLocalizedInstruction(1, point.getOsmIdentifier(), streetNameList)));
        }

    }
}
