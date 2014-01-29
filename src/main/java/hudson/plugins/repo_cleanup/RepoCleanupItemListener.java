package hudson.plugins.repo_cleanup;

import hudson.model.BuildableItemWithBuildWrappers;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;


public class RepoCleanupItemListener extends ItemListener {

    @Override
    public void onCreated(Item item) {
        super.onCreated(item);
        if (item instanceof BuildableItemWithBuildWrappers) {
            BuildableItemWithBuildWrappers itemWithWrapper = (BuildableItemWithBuildWrappers)item;
            itemWithWrapper.getBuildWrappersList().add(new PreBuildCleanup());
        }
    }

}
