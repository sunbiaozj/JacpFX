/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [PerspectiveRegistry.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */

package org.jacpfx.rcp.registry;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.rcp.util.FXUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;

/**
 * Created with IntelliJ IDEA.
 * User: Andy
 * Date: 28.05.13
 * Time: 21:13
 * Global registry with references to all perspective
 */
public class PerspectiveRegistry {
    private static final Map<String, Perspective<Node, EventHandler<Event>, Event, Object>> perspectiveReg = new ConcurrentHashMap<>();
    private static final AtomicReference<String> currentVisiblePerspectiveId = new AtomicReference<>();
    private static final Collector<Perspective<Node, EventHandler<Event>, Event, Object>, ?, TreeSet<Perspective<Node, EventHandler<Event>, Event, Object>>> collector = Collector.of(TreeSet::new, TreeSet::add,
            (left, right) -> {
                left.addAll(right);
                return left;
            });


    private PerspectiveRegistry() {

    }

    /**
     * clears registry on application shutdown
     */
    public static void clearOnShutdown() {
        perspectiveReg.clear();
        ;
    }

    /**
     * Set a new perspective id and returns the current id.
     *
     * @param id, the new perspective id
     * @return the previous perspective id
     */
    public static String getAndSetCurrentVisiblePerspective(final String id) {
        return currentVisiblePerspectiveId.getAndSet(id);
    }


    /**
     * returns the current visible perspective id.
     *
     * @return the current visible perspective id
     */
    public static String getCurrentVisiblePerspective() {
        return currentVisiblePerspectiveId.get();
    }



    /**
     * Registers a perspective.
     *
     * @param perspective, a perspective to register
     */
    public static void registerPerspective(
            final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        Objects.requireNonNull(perspective.getContext());
        perspectiveReg.putIfAbsent(perspective.getContext().getId(), perspective);
    }

    /**
     * Removes perspective from registry.
     *
     * @param perspective, a perspective to remove
     */
    public static void removePerspective(
            final Perspective<Node, EventHandler<Event>, Event, Object> perspective) {
        Objects.requireNonNull(perspective.getContext());
        perspectiveReg.remove(perspective.getContext().getId());
    }

    /**
     * Returns the next active perspective. This can happen when a perspective was set to inactive. In this case the next underlying perspective should be displayed.
     *
     * @param current the current active perspective
     * @return the next active perspective
     */
    public static Optional<Perspective<Node, EventHandler<Event>, Event, Object>> findNextActivePerspective(final Perspective<Node, EventHandler<Event>, Event, Object> current) {
        return Optional.ofNullable(getNextValidPerspective(perspectiveReg.values(), current));
    }

    /**
     * Return an active perspective
     *
     * @param p,       The List with all Perspectives
     * @param current, the current perspective
     * @return the next valid perspective
     */
    private static Perspective<Node, EventHandler<Event>, Event, Object> getNextValidPerspective(final Collection<Perspective<Node, EventHandler<Event>, Event, Object>> p, final Perspective<Node, EventHandler<Event>, Event, Object> current) {
        final TreeSet<Perspective<Node, EventHandler<Event>, Event, Object>> allActive = p.stream()
                .filter(active -> active.getContext().isActive() || active.equals(current))
                .collect(collector);
        return selectCorrectPerspective(current, allActive);
    }

    private static Perspective<Node, EventHandler<Event>, Event, Object> selectCorrectPerspective(final Perspective<Node, EventHandler<Event>, Event, Object> current, final NavigableSet<Perspective<Node, EventHandler<Event>, Event, Object>> allActive) {
        Perspective<Node, EventHandler<Event>, Event, Object> targetId = allActive.higher(current);
        if (targetId == null) targetId = allActive.lower(current);
        if (targetId == null) return null;
        return targetId;
    }


    /**
     * Returns a perspective by perspectiveId
     *
     * @param targetId , the target perspective id
     * @return a perspective
     */
    public static Perspective<Node, EventHandler<Event>, Event, Object> findPerspectiveById(
            final String targetId) {
        return perspectiveReg.get(targetId);
    }

    /**
     * Returns a perspective by perspectiveId
     *
     * @param componentId , the target perspective id
     * @param parentId    , the target workbench id
     * @return a perspective
     */
    public static Perspective<Node, EventHandler<Event>, Event, Object> findPerspectiveById(
            final String parentId, final String componentId) {
        return findPerspectiveById(FXUtil.getQualifiedComponentId(parentId,componentId));
    }


    /**
     * Checks if a specific componentId is present in defined perspective annotation. This method call assumes that a check for component instances for this perspective was already done
     *
     * @param parentId    The perspective ID
     * @param componentId The component ID
     * @return True if component exists in perspective
     */
    public static boolean perspectiveContainsComponentIdInAnnotation(final String parentId, final String componentId) {
        final Perspective<Node, EventHandler<Event>, Event, Object> perspective = findPerspectiveById(parentId);
        if (perspective == null) return false;
        final Class perspectiveClass = perspective.getPerspective().getClass();
        if (!perspectiveClass.isAnnotationPresent(org.jacpfx.api.annotations.perspective.Perspective.class))
            return false;
        final org.jacpfx.api.annotations.perspective.Perspective annotation = (org.jacpfx.api.annotations.perspective.Perspective) perspectiveClass.getAnnotation(org.jacpfx.api.annotations.perspective.Perspective.class);
        return containsComponentInAnnotation(annotation, componentId);
    }

    private static boolean containsComponentInAnnotation(final org.jacpfx.api.annotations.perspective.Perspective annotation, final String componentId) {
        final String[] componentIds = annotation.components();
        Arrays.sort(componentIds);
        return Arrays.binarySearch(componentIds, componentId) >= 0;
    }


}
