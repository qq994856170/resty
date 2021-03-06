package cn.dreampie.route.core;

import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;
import cn.dreampie.route.handler.Handler;
import cn.dreampie.route.http.HttpRequest;
import cn.dreampie.route.http.HttpResponse;
import cn.dreampie.route.http.HttpStatus;
import cn.dreampie.route.http.exception.WebException;
import cn.dreampie.route.render.RenderFactory;

/**
 * ActionHandler
 */
public final class RouteHandler extends Handler {

  private final RouteBuilder resourceBuilder;
  private static final Logger logger = LoggerFactory.getLogger(RouteHandler.class);

  public RouteHandler(RouteBuilder resourceBuilder) {
    this.resourceBuilder = resourceBuilder;
  }

  /**
   * handle
   */
  public final void handle(HttpRequest request, HttpResponse response, boolean[] isHandled) {

    RouteMatch routeMatch = null;
    Route route = null;
    isHandled[0] = true;

    for (Route r : resourceBuilder.getRoutes()) {
      routeMatch = r.match(request);
      if (routeMatch != null) {
        route = r;
        break;
      }
    }

    if (routeMatch != null) {
      response.setStatus(HttpStatus.OK);
      try {
        routeMatch.getRender().render(request, response, new RouteInvocation(route, routeMatch).invoke());
      } catch (Exception e) {
        throw new WebException(e.getMessage());
      }
    } else {
      // no route matched
      String path = request.getRestPath();
      StringBuilder sb = new StringBuilder()
          .append("No rest route found for ")
          .append(request.getHttpMethod()).append(" ").append(path).append("\n");

      sb.append("routes:\n")
          .append("-----------------------------------\n");
      for (Route router : resourceBuilder.getRoutes()) {
        sb.append(router).append("\n");
      }
      sb.append("-----------------------------------");
      response.setStatus(HttpStatus.NOT_FOUND);
      RenderFactory.getRender("text").render(request, response, sb);
    }
  }
}





