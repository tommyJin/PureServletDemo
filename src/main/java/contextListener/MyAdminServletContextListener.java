package contextListener;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServletContextListener;
/**
 * Class was needed for implementing metrics into embedded server. You need a registry for all your metrics
 * in Metrics v.3.0
 * @author joayers
 *
 */
@SuppressWarnings("deprecation")
public class MyAdminServletContextListener extends AdminServletContextListener {
    public static final MetricRegistry registry = new MetricRegistry();
    public static final HealthCheckRegistry Hregistry = new HealthCheckRegistry();

    @Override
    protected MetricRegistry getMetricRegistry() {
        return registry;
    }

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return Hregistry;
    }
}