package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.*;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /**
     * overlay used to display bus route legend text on a layer above the map
     */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /**
     * overlays used to plot bus routes
     */
    private List<Polyline> busRouteOverlays;

    private float lineWidth;
    private Route route;
    private int index;


    /**
     * Constructor
     *
     * @param context the application context
     * @param mapView the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    public void plotRoutes(int zoomLevel) {
        busRouteOverlays.clear();
        busRouteLegendOverlay.clear();
        updateVisibleArea();
        lineWidth = getLineWidth(zoomLevel);
        for (Route r : StopManager.getInstance().getSelected().getRoutes()) {
            route = r;
            for (RoutePattern rp : r.getPatterns()) {
                makePolylines(rp);
            }
        }
    }

    private void makePolylines(RoutePattern rp) {

        index = 0;
        while (index < rp.getPath().size()) {

            Polyline p = new Polyline(context);
            p.setWidth(lineWidth);
            try {
                int color = busRouteLegendOverlay.getColor(route.getNumber());
                p.setColor(color);
            } catch (NullPointerException e) {
                p.setColor(busRouteLegendOverlay.add(route.getNumber()));
            }
            List<LatLon> latLons;
            if (index != 0) {
                latLons = rp.getPath().subList(index - 1, rp.getPath().size());
            } else {
                latLons = rp.getPath();
            }
            p.setPoints(getPoints(latLons));
            busRouteOverlays.add(p);
        }

    }

    private List<GeoPoint> getPoints(List<LatLon> latLons) {

        LatLon before = null;
        List<GeoPoint> points = new ArrayList<>();

        for (LatLon ll : latLons) {
            if (before != null) {
                if (Geometry.rectangleIntersectsLine(northWest, southEast, before, ll)) {
                    if (!points.contains(Geometry.gpFromLatLon(before))) {
                        points.add(Geometry.gpFromLatLon(before));
                    }
                    points.add(Geometry.gpFromLatLon(ll));
                } else if (Geometry.rectangleContainsPoint(northWest, southEast, ll)) {
                    points.add(Geometry.gpFromLatLon(ll));
                } else {
                    return points;
                }
            }
            before = ll;
            index++;
        }
        return points;
    }


    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     *
     * @param zoomLevel the zoom level of the map
     * @return width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if (zoomLevel > 14) {
            return 7.0f * BusesAreUs.dpiFactor();
        } else if (zoomLevel > 10) {
            return 5.0f * BusesAreUs.dpiFactor();
        } else {
            return 2.0f * BusesAreUs.dpiFactor();
        }
    }
}
