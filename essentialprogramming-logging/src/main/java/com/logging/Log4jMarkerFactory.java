package com.logging;

import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.status.StatusLogger;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Log4jMarkerFactory implements IMarkerFactory {

    private final MarkerMap markerMap = MarkerMap.getInstance();

    public Log4jMarkerFactory() {
    }

    public Log4jMarker getMarker(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Marker name must not be null");
        }
        final Log4jMarker marker = this.markerMap.get(name);

        return marker != null
                ? marker
                : this.addMarkerIfAbsent(name, MarkerManager.getMarker(name), null);
    }

    public Log4jMarker getMarker(final String name, final Object value) {
        if (name == null) {
            throw new IllegalArgumentException("Marker name must not be null");
        }
        final Log4jMarker marker = this.markerMap.get(name);

        return marker != null
                ? marker
                : this.addMarkerIfAbsent(name, MarkerManager.getMarker(name), value);
    }

    public Log4jMarker getMarker(final Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("Marker must not be null");
        }

        final Log4jMarker m = this.markerMap.get(marker.getName());
        return m != null ? m : this.addMarkerIfAbsent(marker.getName(), convertMarker(marker), null);
    }

    private Log4jMarker addMarkerIfAbsent(final String name,
                                  final org.apache.logging.log4j.Marker log4jMarker,
                                  final Object value) {

        final Log4jMarker marker = new Log4jMarker(this, log4jMarker, value);
        this.markerMap.putIfAbsent(name, marker);

        return this.markerMap.get(name);
    }



    private static org.apache.logging.log4j.Marker convertMarker(final Marker original) {
        if (original == null) {
            throw new IllegalArgumentException("Marker must not be null");
        }

        return convertMarker(original, new ArrayList<>());

    }

    private static org.apache.logging.log4j.Marker convertMarker(final Marker original, final Collection<Marker> visited) {
        org.apache.logging.log4j.Marker marker = MarkerManager.getMarker(original.getName());
        if (original.hasReferences()) {
            Iterator<Marker> it = original.iterator();

            while (it.hasNext()) {
                Marker next = it.next();
                if (visited.contains(next)) {
                    StatusLogger.getLogger().warn("Found a cycle in Marker [{}]. Cycle will be broken.", next.getName());
                } else {
                    visited.add(next);
                    marker.addParents(convertMarker(next, visited));
                }
            }
        }

        return marker;
    }

    public boolean exists(final String name) {
        return this.markerMap.containsKey(name);
    }

    public boolean detachMarker(final String name) {
        return false;
    }

    public Marker getDetachedMarker(final String name) {
        StatusLogger.getLogger().warn("Log4j does not support detached Markers. Returned Marker [{}] will be unchanged.", name);
        return this.getMarker(name);
    }
}
