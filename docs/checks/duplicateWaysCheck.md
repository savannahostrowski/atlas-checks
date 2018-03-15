# Duplicate Ways Check

#### Description

This check identifies Edges which have duplicates. Each Segment in each Edge is checked to identify 
if the Edge is at all duplicated. The Edge is flagged if any Segment duplicated - this includes
partially and completely duplicated Edges.

#### Live Example
The following examples illustrate cases where Edges have duplicates.
1) This edge [id:536929208](https://www.openstreetmap.org/way/536929208) should be flagged as a
result of it being a partially duplicated Edge.
2) This edge [id:24628387](https://www.openstreetmap.org/way/24628387) should be flagged as a result
of it being a partially duplicated Edge.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
We’ll use this information to filter our potential flag candidates.

Our first goal is to validate the incoming Atlas Object. We know two things about roundabouts:
* Must be a valid Edge
* Must have not already been flagged

```java
    public boolean validCheckForObject(final AtlasObject object)
    {
            return object instanceof Edge && !this.isFlagged(object.getIdentifier());
    }

```

After the preliminary filtering of features, we take each Edge and use a series of conditional
statements to validate whether we do in fact want to flag the feature for inspection.


```java
    protected Optional<CheckFlag> flag(final AtlasObject object)
        {
            // Get current edge object
            final Edge edge = (Edge) object;
    
            if (!HighwayTag.isCarNavigableHighway(edge))
            {
                return Optional.empty();
            }
    
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
    
                    if (globalSegments.get(segment).size() > 1
                            && !this.isFlagged(edge.getOsmIdentifier()))
                    {
                        this.markAsFlagged(edge.getOsmIdentifier());
                        return Optional.of(this.createFlag(edge, this.getLocalizedInstruction(0,
                                edge.getOsmIdentifier(), numberOfDuplicates - 1)));
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

```


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
```

>>>>>>> updates to docs




To learn more about the code, please look at the comments in the source code for the check.
<<<<<<< HEAD
[DuplicateWaysCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/DuplicateWaysCheck.java)
=======
[DuplicateWaysCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/ultiFeatureRoundaboutCheck.jMava)
>>>>>>> updates to docs
