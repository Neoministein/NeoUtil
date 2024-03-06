package com.neo.util.framework.rest.web.htmx;

import com.neo.util.common.impl.html.HtmlElement;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.excpetion.ToExternalException;
import com.neo.util.framework.impl.json.JsonSchemaLoader;
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
@ToExternalException({JsonSchemaLoader.E_SCHEMA_DOES_NOT_EXIST})
public class JsonSchemaHtmxResource {

    public static final String RESOURCE_LOCATION = "/admin/html/json-schema";

    protected JsonSchemaLoader jsonSchemaLoader;

    @Inject
    public JsonSchemaHtmxResource(JsonSchemaLoader jsonSchemaLoader) {
        this.jsonSchemaLoader = jsonSchemaLoader;
    }

    @GET
    public HtmlElement getMainTable() {
        return HTML.
                """
                <h5 class="card-title">Json Schema</h5>
                <div class="row">
                    <div class="col-6" style="padding-top: 1.3rem!important;">
                        <table id="main" class="table table-striped">
                            <tbody>
                                \{jsonSchemaLoader.getUnmodifiableMap().keySet().stream().sorted().map(this::getTableElement)}
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
        String rawJson = jsonSchemaLoader.requestJsonSchema(path)
                //Substring +6 is done since default toString start with '"#" :' which isn't valid json
                .toString().substring(6);


        return JsonUtil.toPrettyJson(JsonUtil.fromJson(rawJson));
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
