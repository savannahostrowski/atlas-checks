package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check looks for duplicate edges or edge segments.  One copy of the duplicate segment will
 * be flagged for review.
 *
 * @author savannahostrowski
 */
public class DuplicateWaysCheck extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    public static final String DUPLICATE_EDGE_INSTRUCTIONS = "This way, {0, number, #}, has "
            + "is a duplicate, has a duplicate edge segment, or is part of a duplicate segment. "
            + "There are {1, number, #}";
    public static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(DUPLICATE_EDGE_INSTRUCTIONS);

    // a map of segments and list of Edge identifiers
    private final Map<Segment, Set<Long>> globalSegments = new HashMap<>();
    private final Map<Edge, List<Segment>> edgeToSegment = new HashMap<>();

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
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
        // Get current edge object
        final Edge edge = (Edge) object;

        // Get the edge identifier
        final long identifier = edge.getIdentifier();

        // Get all Segments in Edge
        final List<Segment> edgeSegments = edge.asPolyLine().segments();

        // For each Segment in the Edge
        for (final Segment segment : edgeSegments)
        {
            // Check if the Segment is in globalSegments
            if (globalSegments.containsKey(segment))
            {
                // add identifier to the list of identifiers with that segment
                globalSegments.get(segment).add(identifier);

                final int numberOfDuplicates = globalSegments.get(segment).size();

                if (globalSegments.get(segment).size() > 1 && !this.isFlagged(edge.getOsmIdentifier())) {
                    this.markAsFlagged(edge.getOsmIdentifier());
                    return Optional.of(this.createFlag(edge,
                            this.getLocalizedInstruction(0, edge.getOsmIdentifier(), numberOfDuplicates - 1)));

                }
            }
            else
            {
                // if it doesn't already exist, then add the segment and list with one identifier
                final Set<Long> identifiers = new HashSet<>();
                identifiers.add(identifier);
                globalSegments.put(segment, identifiers);
            }
        }


        return Optional.empty();
    }


}
