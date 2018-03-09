package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.*;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author savannahostrowski
 */
public class DuplicateWaysCheck extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    public static final String DUPLICATE_EDGE_INSTRUCTIONS = "This way, {0, number, integer}, has "
            + "is a duplicate or has a duplicate edge segment.";
    public static final List<String> FALLBACK_INSRUCTIONS = Arrays.asList(
           DUPLICATE_EDGE_INSTRUCTIONS);

    public static final Map<Long, List<Segment>> edges = new HashMap<>();
    public static final Iterator iterator = edges.entrySet().iterator();

    @Override
    protected List<String> getFallbackInsructions() {
        return FALLBACK_INSRUCTIONS;
    }

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public DuplicateWaysCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
       return object instanceof Edge && !this.isFlagged(object.getIdentifier());
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

        // Cast Atlas object to Edge
        final Edge edge = (Edge) object;
        // Get Edge as a List of Segments
        final List<Segment> currentEdgeSegments = edge.asPolyLine().segments();

        // For each entry in the map of seen Edges
        while (iterator.hasNext()) {

            final Map.Entry pair = (Map.Entry) iterator.next();
            final Edge mapEdge = (Edge) pair.getValue();
            final List<Segment> mapEdgeSegments = mapEdge.asPolyLine().segments();

            if (mapEdgeSegments.containsAll(currentEdgeSegments)) {

            }

            if (mapEdgeSegments.contains(currentEdgeSegments)) {

            }

            edges.put(mapEdge.getIdentifier(), mapEdgeSegments);

        }
        return Optional.empty();
    }


}
