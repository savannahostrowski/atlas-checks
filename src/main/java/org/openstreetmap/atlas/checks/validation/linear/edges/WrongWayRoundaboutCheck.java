package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags roundabouts where the directionality is opposite to what it should be.
 *
 * @author savannahostrowski
 */

public class WrongWayRoundaboutCheck extends BaseCheck
{
    private static final long serialVersionUID = -3018101860747289836L;
    public static final String WRONG_WAY_INSTRUCTIONS = "This roundabout, {0,number,#},"
            + " is going the wrong direction.";
    public static final Set<String> LEFT_DRIVING_COUNTRIES = new HashSet<>(
            Arrays.asList("AIA", "ATG", "AUS", "BHS", "BGD", "BRB", "BMU", "BTN", "BWA", "BRN",
                    "CYM", "CXR", "CCK", "COK", "CYP", "DMA", "FLK", "FJI", "GRD", "GGY", "GUY",
                    "HKG", "IND", "IDN", "IRL", "IMN", "JAM", "JPN", "JEY", "KEN", "KIR", "LSO",
                    "MAC", "MWI", "MYS", "MDV", "MLT", "MUS", "MSR", "MOZ", "NAM", "NRU", "NPL",
                    "NZL", "NIU", "NFK", "PAK", "PNG", "PCN", "SHN", "KNA", "LCA", "VCT", "WSM",
                    "SYC", "SGP", "SLB", "ZAF", "SGS", "LKA", "SUR", "SWZ", "TZA", "THA", "TKL",
                    "TON", "TTO", "TCA", "TUV", "UGA", "GBR", "VGB", "VIR", "ZMB", "ZWE"));
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(WRONG_WAY_INSTRUCTIONS);

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public WrongWayRoundaboutCheck(final Configuration configuration)
    {
        super(configuration);

    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && JunctionTag.isRoundabout(object)
                && !this.isFlagged(object.getIdentifier()) && ((Edge) object).isMasterEdge();
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
        final Edge edge = (Edge) object;

        // Get all edges in the roundabout
        final List<Edge> roundaboutEdges = getAllRoundaboutEdges(edge);

        final Edge firstEdge = roundaboutEdges.get(0);
        final Edge secondEdge = roundaboutEdges.get(1);
        final double firstEdgeHeading = firstEdge.overallHeading().get().asDegrees();
        final double secondEdgeHeading = secondEdge.overallHeading().get().asDegrees();
        final String isoCountryCode = firstEdge.tag(ISOCountryTag.KEY).toUpperCase();
        final boolean isCounterClockwise = firstEdgeHeading > secondEdgeHeading || (firstEdgeHeading <= 90 && secondEdgeHeading >= 270);
        final boolean isLeftDrivingCountry = LEFT_DRIVING_COUNTRIES.contains(isoCountryCode);

        if ((!isCounterClockwise && !isLeftDrivingCountry)
                || (isCounterClockwise && isLeftDrivingCountry))
        {
            return Optional.of(this.createFlag(new HashSet<>(roundaboutEdges),
                    this.getLocalizedInstruction(0, firstEdge.getOsmIdentifier())));
        }

        return Optional.empty();
    }

    /**
     * This method gets all edges in a roundabout given one edge in that roundabout, in ascending
     * Edge identifier order.
     *
     * @param edge
     *            An Edge object known to be a roundabout edge
     * @return A list of edges in the roundabout
     */
    private List<Edge> getAllRoundaboutEdges(final Edge edge)
    {
        final List<Edge> roundaboutEdges = new ArrayList<>();

        // Initialize a queue to add yet to be processed connected edges to
        final Queue<Edge> queue = new LinkedList<>();

        // Mark the current Edge as visited and enqueue it
        this.markAsFlagged(edge.getIdentifier());
        queue.add(edge);

        // As long as the queue is not empty
        while (!queue.isEmpty())
        {
            // Dequeue a connected edge and add it to the roundaboutEdges
            final Edge currentEdge = queue.poll();

            roundaboutEdges.add(currentEdge);

            // Get the edges connected to the edge e as an iterator
            final Set<Edge> connectedEdges = currentEdge.connectedEdges();

            for (final Edge connectedEdge : connectedEdges)
            {
                final Long edgeId = connectedEdge.getIdentifier();

                if (JunctionTag.isRoundabout(connectedEdge)
                        && !roundaboutEdges.contains(connectedEdge))

                {
                    this.markAsFlagged(edgeId);
                    queue.add(connectedEdge);
                }
            }
        }
        roundaboutEdges.sort(Edge::compareTo);
        return roundaboutEdges;
    }
}
