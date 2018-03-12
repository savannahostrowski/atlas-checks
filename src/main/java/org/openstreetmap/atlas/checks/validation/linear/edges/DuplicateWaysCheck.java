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

    private final Map<Segment, Set<Long>> globalSegments = new HashMap<>();


    @Override
    protected List<String> getFallbackInsructions() {
        return FALLBACK_INSRUCTIONS;
    }


    public DuplicateWaysCheck(final Configuration configuration)
    {
        super(configuration);
    }


    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
       return object instanceof Edge && !this.isFlagged(object.getIdentifier());
    }


    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        final long identifier = edge.getIdentifier();
        final List<Segment> edgeSegments = edge.asPolyLine().segments();

        for (Segment segment: edgeSegments) {
            if (globalSegments.containsKey(segment)) {
                // add identifier to the list of identifiers with that segment
                globalSegments.get(segment).add(identifier);

            } else {
                // if it doesn't already exist, then add the segment and list with one identifier
                Set<Long> identifiers = new HashSet<>();
                identifiers.add(identifier);
                globalSegments.put(segment, identifiers);
            }

        }







        return Optional.empty();
    }

}
