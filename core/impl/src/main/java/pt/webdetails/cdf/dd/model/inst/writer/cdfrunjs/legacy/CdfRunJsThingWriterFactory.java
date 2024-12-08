/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.legacy;

import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.CodeComponent;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.CdfRunJsCodeComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy.CdfRunJsGenericComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy.CdfRunJsWidgetComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy.CdfRunJsDateParameterComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy.CdfRunJsExpressionParameterComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy.CdfRunJsParameterComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.CdfRunJsDashboardWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties.CdfRunJsCdaDataSourcePropertyBindingWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties.CdfRunJsDataSourcePropertyBindingWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties.CdfRunJsGenericPropertyBindingWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties.CdfRunJsJFreeChartDataSourcePropertyBindingWriter;

public class CdfRunJsThingWriterFactory implements IThingWriterFactory  {

  @Override
  public IThingWriter getWriter( Thing t ) throws UnsupportedThingException {
    if ( t == null ) {
      throw new IllegalArgumentException( "t" );
    }

    String kind = t.getKind();

    if ( KnownThingKind.Component.equals( kind ) ) {
      return getComponentWriter( t );
    } else if ( KnownThingKind.PropertyBinding.equals( kind ) ) {
      return getPropertyBindingWriter( t );
    } else if ( KnownThingKind.Dashboard.equals( kind ) ) { // shouldn't get here anymore
      return getDashboardWriter( ( (Dashboard) t ) );
    }

    throw new UnsupportedThingException( kind, t.getId() );
  }

  public CdfRunJsDashboardWriter getDashboardWriter( Dashboard dashboard ) {
    return new CdfRunJsDashboardWriter( dashboard.getWcdf().getParsedRendererType() );
  }

  private IThingWriter getComponentWriter( Thing t ) {
    Class compClass = t.getClass();

    if ( GenericComponent.class.isAssignableFrom( compClass ) ) {
      if ( WidgetComponent.class.isAssignableFrom( compClass ) ) {
        return new CdfRunJsWidgetComponentWriter();
      }

      return new CdfRunJsGenericComponentWriter();
    }

    if ( ParameterComponent.class.isAssignableFrom( compClass ) ) {
      return getParameterWriter( t );
    }

    if ( CodeComponent.class.isAssignableFrom( compClass ) ) {
      return new CdfRunJsCodeComponentWriter();
    }

    return null;
  }

  private IThingWriter getParameterWriter( Thing t ) {
    ParameterComponent paramComp = (ParameterComponent) t;
    String typeName = paramComp.getMeta().getName().toLowerCase();

    IThingWriter parameterWriter;
    switch ( typeName ) {
      case SIMPLE_PARAMETER:
      case OLAP_PARAMETER:
        parameterWriter = new CdfRunJsParameterComponentWriter();
        break;
      case DATE_PARAMETER:
        parameterWriter = new CdfRunJsDateParameterComponentWriter();
        break;
      case JS_EXPRESSION_PARAMETER:
        parameterWriter = new CdfRunJsExpressionParameterComponentWriter();
        break;
      default:
        parameterWriter = null;
    }

    return parameterWriter;
  }

  private IThingWriter getPropertyBindingWriter( Thing t ) {
    PropertyBinding propBind = (PropertyBinding) t;
    String propName = propBind.getName().toLowerCase();

    if ( propName.equals( "datasource" ) ) {
      return new CdfRunJsDataSourcePropertyBindingWriter();
    }
    if ( propName.equals( "cdadatasource" ) ) {
      return new CdfRunJsCdaDataSourcePropertyBindingWriter();
    }
    if ( propName.equals( "jfreechartdatasource" ) ) {
      return new CdfRunJsJFreeChartDataSourcePropertyBindingWriter();
    }

    return new CdfRunJsGenericPropertyBindingWriter();
  }
}
