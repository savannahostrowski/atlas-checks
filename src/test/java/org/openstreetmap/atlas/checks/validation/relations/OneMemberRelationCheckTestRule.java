package org.openstreetmap.atlas.checks.validation.relations;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * {@link OneMemberRelationCheckTest} data generator
 *
 * @author savannahostrowski
 */
public class OneMemberRelationCheckTestRule extends CoreTestRule
{
    private static final String ONE = "18.4360044, -71.7194204";
    private static final String TWO = "18.4360737, -71.6970306";
    private static final String THREE = "18.4273807, -71.7052283";

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)) },
            // edges
            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=road" }),
                    @Edge(id = "23", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=road" }),
                    @Edge(id = "31", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }, tags = { "highway=road" }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_FROM),
                    @Member(id = "2", type = "node", role = RelationTypeTag.RESTRICTION_ROLE_VIA),
                    @Member(id = "23", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_TO) },
                    tags = { "restriction=no_u_turn" }) })
    private Atlas validRelation;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)) },
            // edges
            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=road" }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_FROM)},
                    tags = { "restriction=no_u_turn" }) })
    private Atlas oneMemberRelation;

    public Atlas getValidRelation() {
        return this.validRelation;
    }

    public Atlas getOneMemberRelation() {
        return this.oneMemberRelation;
    }
}