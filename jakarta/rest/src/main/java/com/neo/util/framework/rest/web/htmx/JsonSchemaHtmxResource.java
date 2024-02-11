package com.neo.util.framework.rest.web.htmx;

import com.neo.util.common.impl.html.HtmlElement;
import com.neo.util.framework.rest.web.htmx.navigation.HtmxNavigationElement;
import com.neo.util.framework.rest.web.rest.JsonSchemaResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static com.neo.util.common.impl.html.HtmlStringTemplate.HTML;

@ApplicationScoped
@Path(JsonSchemaHtmxResource.RESOURCE_LOCATION)
@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
@HtmxNavigationElement(name = "Json Schema", route = JsonSchemaHtmxResource.RESOURCE_LOCATION)
public class JsonSchemaHtmxResource {

    public static final String RESOURCE_LOCATION = "/admin/html/json-schema";

    @Inject
    protected JsonSchemaResource jsonSchemaResource;

    @GET
    public HtmlElement getMainTable() {
        //language=HTML
        return HTML.
                """
                <h5 class="card-title">Json Schema</h5>
                <div class="row">
                    <div class="col-6" style="padding-top: 1.3rem!important;">
                        <table id="main" class="table table-striped">
                            <tbody>
                                \{jsonSchemaResource.getSchemaNames().stream().map(this::getTableElement)}
                            </tbody>
                        </table>
                    </div>
                    <div class="col-6">
                        <pre>
                            <code id="json-content" class="language-json">

                            </code>
                        </pre>
                    </div>
                </div>
                <link rel="stylesheet" href="https://unpkg.com/@highlightjs/cdn-assets@11.9.0/styles/default.min.css">
                <script src="https://unpkg.com/@highlightjs/cdn-assets@11.9.0/highlight.min.js"></script>
                <script>
                    document.addEventListener('htmx:afterSwap', function(event) {
                        // Check if the swapped element is the container
                        if (event.detail.target.id === 'json-content') {
                            // Execute your JavaScript function here after the swap is complete
                            hljs.highlightAll();
                        }
                    });
                    </script>
                """;
    }

    @GET
    @Path("{path : .+}")//This param'{path : .+}' means that it also includes '/'
    public String getSchema(@PathParam("path") String path) {
        return jsonSchemaResource.getSchema(path);
    }

    public HtmlElement getTableElement(String path) {
        return HTML."""
                <tr>
                    <td hx-trigger="click"
                        hx-get="\{RESOURCE_LOCATION + "/" + path}"
                        hx-target="#json-content">
                    \{path}
                    </td>
                </tr>
                """;
    }

}
