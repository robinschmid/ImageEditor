/*
 * Copyright 2006-2015 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package net.rs.lamsi.general.myfreechart.gestures.def;

import net.rs.lamsi.general.myfreechart.gestures.ChartGesture;
import net.rs.lamsi.general.myfreechart.gestures.ChartGesture.Button;
import net.rs.lamsi.general.myfreechart.gestures.ChartGesture.Entity;
import net.rs.lamsi.general.myfreechart.gestures.ChartGesture.Event;
import net.rs.lamsi.general.myfreechart.gestures.ChartGesture.Key;
import net.rs.lamsi.general.myfreechart.gestures.ChartGestureHandler;
import net.rs.lamsi.general.myfreechart.gestures.ChartGestureHandler.Handler;
import net.rs.lamsi.general.myfreechart.gestures.interf.GestureHandlerFactory;

/**
 * Definition of {@link ChartGestureHandler}s Used to store the definition and to generate
 * GestureHandlers
 * 
 * @author Robin Schmid (robinschmid@uni-muenster.de)
 */
public class GestureHandlerDef implements GestureHandlerFactory {

  protected Handler handler;
  protected Entity entity;
  protected Event[] event;
  protected Button button;
  protected Key key;
  protected Object[] param;

  public GestureHandlerDef(Handler handler, Entity entity, Event[] event, Button button, Key key,
      Object[] param) {
    super();
    this.handler = handler;
    this.entity = entity;
    this.event = event;
    this.button = button;
    this.key = key;
    this.param = param;
  }

  @Override
  public ChartGestureHandler createHandler() {
    return ChartGestureHandler.createHandler(handler, new ChartGesture(entity, event, button, key),
        param);
  }

  public Handler getHandler() {
    return handler;
  }

  public Entity getEntity() {
    return entity;
  }

  public Event[] getEvent() {
    return event;
  }

  public Button getButton() {
    return button;
  }

  public Key getKey() {
    return key;
  }

  public Object[] getParam() {
    return param;
  }

  public void setHandler(Handler handler) {
    this.handler = handler;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public void setEvent(Event[] event) {
    this.event = event;
  }

  public void setButton(Button button) {
    this.button = button;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public void setParam(Object[] param) {
    this.param = param;
  }

}
