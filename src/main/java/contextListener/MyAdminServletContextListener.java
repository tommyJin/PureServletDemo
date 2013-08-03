package contextListener;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServletContextListener;

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