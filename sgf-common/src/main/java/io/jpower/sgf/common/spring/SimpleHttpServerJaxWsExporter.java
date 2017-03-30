package io.jpower.sgf.common.spring;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.jaxws.AbstractJaxWsServiceExporter;
import org.springframework.remoting.jaxws.SimpleJaxWsServiceExporter;

import io.jpower.sgf.thread.NamedThreadFactory;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

/**
 * 修改 org.springframework.remoting.jaxws.SimpleHttpServerJaxWsServiceExporter
 * <p>
 * Simple exporter for JAX-WS services, autodetecting annotated service beans
 * (through the JAX-WS {@link javax.jws.WebService} annotation) and exporting
 * them through the HTTP server included in Sun's JDK 1.6. The full address for
 * each service will consist of the server's base address with the service name
 * appended (e.g. "http://localhost:8080/OrderService").
 * <p>
 * <p>
 * Note that this exporter will only work on Sun's JDK 1.6 or higher, as well as
 * on JDKs that ship Sun's entire class library as included in the Sun JDK. For
 * a portable JAX-WS exporter, have a look at {@link SimpleJaxWsServiceExporter}
 * .
 *
 * @author Juergen Hoeller
 * @see javax.jws.WebService
 * @see javax.xml.ws.Endpoint#publish(Object)
 * @see SimpleJaxWsServiceExporter
 * @since 2.5.5
 */
@SuppressWarnings("restriction")
public class SimpleHttpServerJaxWsExporter extends AbstractJaxWsServiceExporter {

    protected final Log logger = LogFactory.getLog(getClass());

    private HttpServer server;

    private int port = -1;

    private String hostname;

    private int backlog = -1;

    private int shutdownDelay = 0;

    private String basePath = "/";

    private List<Filter> filters;

    private Authenticator authenticator;

    private int poolSize;

    private ThreadPoolExecutor exec;

    private boolean localServer = false;

    /**
     * Specify the HTTP server's port. Default is 8080.
     * <p>
     * Only applicable for a locally configured HTTP server. Ignored when the
     * {@link #setServer "server"} property has been specified.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Specify the HTTP server's hostname to bind to. Default is localhost; can
     * be overridden with a specific network address to bind to.
     * <p>
     * Only applicable for a locally configured HTTP server. Ignored when the
     * {@link #setServer "server"} property has been specified.
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Specify the HTTP server's TCP backlog. Default is -1, indicating the
     * system's default value.
     * <p>
     * Only applicable for a locally configured HTTP server. Ignored when the
     * {@link #setServer "server"} property has been specified.
     */
    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    /**
     * Specify the number of seconds to wait until HTTP exchanges have completed
     * when shutting down the HTTP server. Default is 0.
     * <p>
     * Only applicable for a locally configured HTTP server. Ignored when the
     * {@link #setServer "server"} property has been specified.
     */
    public void setShutdownDelay(int shutdownDelay) {
        this.shutdownDelay = shutdownDelay;
    }

    /**
     * Set the base path for context publication. Default is "/".
     * <p>
     * For each context publication path, the service name will be appended to
     * this base address. E.g. service name "OrderService" -> "/OrderService".
     *
     * @see javax.xml.ws.Endpoint#publish(Object)
     * @see javax.jws.WebService#serviceName()
     */
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Register common {@link com.sun.net.httpserver.Filter Filters} to be
     * applied to all detected {@link javax.jws.WebService} annotated beans.
     */
    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    /**
     * Register a common {@link com.sun.net.httpserver.Authenticator} to be
     * applied to all detected {@link javax.jws.WebService} annotated beans.
     */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.port <= 0) {
            throw new IllegalArgumentException("jaxWsPort is invalid: " + this.port);
        }

        if (this.poolSize <= 0) {
            throw new IllegalArgumentException("poolSize is invalid: " + this.poolSize);
        }

        InetSocketAddress address = (this.hostname != null
                ? new InetSocketAddress(this.hostname, this.port)
                : new InetSocketAddress(this.port));
        this.server = HttpServer.create(address, this.backlog);
        // 设置线程池
        ThreadPoolExecutor exec = new ThreadPoolExecutor(this.poolSize, this.poolSize, 30L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory("jax-ws"));
        exec.allowCoreThreadTimeOut(true);
        this.server.setExecutor(exec);
        this.exec = exec;
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Starting HttpServer at address " + address);
        }
        this.server.start();
        this.localServer = true;
        super.afterPropertiesSet();
    }

    @Override
    protected void publishEndpoint(Endpoint endpoint, WebService annotation) {
        endpoint.publish(buildHttpContext(endpoint, annotation.serviceName()));
    }

    @Override
    protected void publishEndpoint(Endpoint endpoint, WebServiceProvider annotation) {
        endpoint.publish(buildHttpContext(endpoint, annotation.serviceName()));
    }

    /**
     * Build the HttpContext for the given endpoint.
     *
     * @param endpoint    the JAX-WS Provider Endpoint object
     * @param serviceName the given service name
     * @return the fully populated HttpContext
     */
    protected HttpContext buildHttpContext(Endpoint endpoint, String serviceName) {
        String fullPath = calculateEndpointPath(endpoint, serviceName);
        HttpContext httpContext = this.server.createContext(fullPath);
        if (this.filters != null) {
            httpContext.getFilters().addAll(this.filters);
        }
        if (this.authenticator != null) {
            httpContext.setAuthenticator(this.authenticator);
        }
        return httpContext;
    }

    /**
     * Calculate the full endpoint path for the given endpoint.
     *
     * @param endpoint    the JAX-WS Provider Endpoint object
     * @param serviceName the given service name
     * @return the full endpoint path
     */
    protected String calculateEndpointPath(Endpoint endpoint, String serviceName) {
        return this.basePath + serviceName;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (this.localServer) {
            logger.info("Stopping HttpServer");
            this.server.stop(this.shutdownDelay);
            this.exec.shutdownNow();
        }
    }

}
