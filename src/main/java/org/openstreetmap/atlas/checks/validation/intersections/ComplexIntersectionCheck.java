package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNodeFinder;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags all Big Nodes, also knows as complex intersections.\
 *
 * @author danielbaah
 */
public class ComplexIntersectionCheck extends BaseCheck
{

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Big Node found for feature {0,number,#}");

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public ComplexIntersectionCheck(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
    }

    @Override public Optional<Finder> finder()
    {
        return Optional.of(new BigNodeFinder());
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
        // by default we will assume all objects as valid
        return object instanceof BigNode;
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
        // insert algorithmic check to see whether object needs to be flagged.
        // Example of flagging an object
        // return Optional.of(this.createFlag(object, "Instruction how to fix issue or reason behind
        // flagging the object");

        final BigNode bigNode = (BigNode) object;

        return Optional
                .of(createFlag(object, this.getLocalizedInstruction(0, bigNode.getOsmIdentifier())));
        
    }
}
