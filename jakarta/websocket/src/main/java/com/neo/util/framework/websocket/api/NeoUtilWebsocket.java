package com.neo.util.framework.websocket.api;

import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.websocket.persistence.SocketLogSearchable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides an active {@link RequestScoped} context and {@link UserRequestDetails} for the default {@link ServerEndpoint}
 * method invocations.
 * <p>
 * Classes Annotated require the following:
 * <ul>
 *  <li> {@link ServerEndpoint#configurator()} must be a derivative class of {@link WebserverHttpHeaderForwarding} </li>
 *  <li> A method annotated with {@link jakarta.websocket.OnOpen} requiring the parameters {@link Session} and {@link EndpointConfig} </li>
 *  <li> A method annotated with {@link jakarta.websocket.OnClose} requiring the parameter {@link Session} </li>
 *  <li> [OPTIONAL] a method annotated with {@link jakarta.websocket.OnMessage} requiring the parameter {@link Session} </li>
 * </ul>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NeoUtilWebsocket {

    /**
     * If the endpoint throughput is monitored with a {@link SocketLogSearchable}
     * <p>
     * Outgoing needs to go through the {@link WebsocketStateContext#broadcastAsync(String)} or {@link WebsocketStateContext#broadcast(String)}
     */
    boolean monitored() default false;

    /**
     * Used to define a websocket which requires authentication.
     * <p>
     * The implementation should check if {@link UserRequestDetails#getUser()} can be resolved
     */
    boolean secured() default false;

    /**
     * List of roles that are permitted access.
     */
    String[] roles() default {};
}
