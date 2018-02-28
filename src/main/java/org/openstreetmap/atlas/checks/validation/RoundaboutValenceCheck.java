package org.openstreetmap.atlas.checks.validation;

import java.util.*;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check ensures that roundabouts with unreasonable valences are flagged. In reference to OSM
 * Wiki, each roundabout should have greater than 1 connection as 1 connection should be tagged as a
 * turning point, and no connections is obviously not a valid way.
 *
 * @author savannahostrowski
 */
public class RoundaboutValenceCheck extends BaseCheck
{

    private static final long serialVersionUID = 1L;
    public static final String WRONG_VALENCE_INSTRUCTIONS = "This roundabout, {0}, has the "
            + "wrong valence";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            WRONG_VALENCE_INSTRUCTIONS);

    @Override
    protected  List<String> getFallbackInstructions() {
        return FALLBACK_INSTRUCTIONS;
    }

    public RoundaboutValenceCheck(final Configuration configuration)
    {
        super(configuration);

    }


    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // We check that the object is an instance of Edge
        return object instanceof Edge
                // and that the Edge has not already been marked as flagged
                && !this.isFlagged(object.getIdentifier())
                // make sure that the edges are instances of roundabout
                && JunctionTag.isRoundabout(object);
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

        // All roundabout edges
        final Map<Long, Edge> roundaboutEdges = new HashMap<>();

        getAllRoundaboutEdges(edge, roundaboutEdges);

        Iterator iterator = roundaboutEdges.entrySet().iterator();
        int totalRoundaboutValence = 0;

        // if the roundabout is already a single features (i.e. it isn't composed of a collection
        // of edges), then we just need to do a basic check for its connections to get the valence
        if (roundaboutEdges.size() == 1) {
            Map.Entry pair = (Map.Entry) iterator.next();
            Edge roundaboutEdge = (Edge) pair.getValue();
            totalRoundaboutValence = roundaboutEdge.connectedEdges().size();

        } else {
            //add if for single feature handling (just connected edges) else do the while loop
            while (iterator.hasNext())
            {
                Map.Entry pair = (Map.Entry) iterator.next();
                Edge roundaboutEdge = (Edge) pair.getValue();

                System.out.println(roundaboutEdge.connectedEdges().size());

                if (roundaboutEdge.connectedEdges().size() > 2)
                {
                    totalRoundaboutValence++;
                }
            }
        }

        // if the roundabout's valence is less than 2 or greater than 10, then we want to flag it
        // for further inspection
        if (totalRoundaboutValence < 2 || totalRoundaboutValence > 10)
        {
            Set<Edge> roundaboutToFlag = new HashSet<>();
            for (Long key : roundaboutEdges.keySet())
            {
                roundaboutToFlag.add(roundaboutEdges.get(key));
            }
            return Optional.of(this.createFlag(roundaboutToFlag,
                    this.getLocalizedInstruction(0, edge.getOsmIdentifier())));
        }
        else
        {
            return Optional.empty();
        }
    }

    private void getAllRoundaboutEdges(Edge edge, Map<Long, Edge> roundaboutEdges)
    {

        // Get the edges connected to the current edge as an iterator
        final Iterator<Edge> connectedEdges = edge.connectedEdges().iterator();

        while (connectedEdges.hasNext())
        {
            final Edge current_edge = connectedEdges.next();
            final Long id = current_edge.getIdentifier();

            if (JunctionTag.isRoundabout(current_edge))
            {
                if (!roundaboutEdges.containsKey(id) && id != edge.getIdentifier()
                        && !this.isFlagged(id))
                {
                    this.markAsFlagged(id);
                    roundaboutEdges.put(id, current_edge);
                    getAllRoundaboutEdges(edge, roundaboutEdges);

                }
            }
        }
    }
}