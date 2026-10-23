package com.macsuite.common;

/**
 * Generic base class for module step definitions.
 * Eliminates lazy-loading boilerplate by managing the module's primary page object.
 *
 * @param <T> the page object type for this module
 */
public abstract class ModuleSteps<T> extends StepBase {

    private T modulePage;

    protected T getModulePage() {
        if (modulePage == null) {
            modulePage = createModulePage();
        }
        return modulePage;
    }

    protected abstract T createModulePage();
}
