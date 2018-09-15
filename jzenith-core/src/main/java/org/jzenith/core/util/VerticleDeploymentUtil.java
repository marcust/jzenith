package org.jzenith.core.util;

import com.englishtown.vertx.guice.GuiceVerticleLoader;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;

@UtilityClass
public class VerticleDeploymentUtil {

    public static DeploymentOptions forGuiceVerticleLoader() {
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(new JsonObject());
        deploymentOptions.getConfig().put(GuiceVerticleLoader.CONFIG_BOOTSTRAP_BINDER_NAME, new JsonArray());
        return deploymentOptions;
    }

}
