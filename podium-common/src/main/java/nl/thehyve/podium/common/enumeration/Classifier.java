package nl.thehyve.podium.common.enumeration;

/**
 * Indicates a type as a status or outcome type, used as classifier, which should be an enum.
 */
public interface Classifier {

    /**
     * The name of the enum value.
     * @return the name of the enum value.
     */
    String name();

}
