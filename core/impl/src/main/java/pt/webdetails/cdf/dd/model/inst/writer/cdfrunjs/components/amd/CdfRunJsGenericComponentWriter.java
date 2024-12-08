/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd;

import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.ExtensionPropertyBinding;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.model.meta.GenericComponentType;
import pt.webdetails.cdf.dd.model.meta.PropertyTypeUsage;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.Utils;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.*;

public class CdfRunJsGenericComponentWriter extends JsWriterAbstract implements IThingWriter {

  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {

    this.write( (StringBuilder) output, (CdfRunJsDashboardWriteContext) context, (GenericComponent) t );

  }

  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, GenericComponent comp )
      throws ThingWriteException {

    final String className = Utils.getComponentClassName( comp.getMeta().getName() );

    this.write( out, context, comp, className );

  }

  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, GenericComponent comp,
                     String className )
      throws ThingWriteException {

    GenericComponentType compType = comp.getMeta();

    String id = comp.getId();

    out.append( "var " ).append( id ).append( " = new " ).append( className ).append( "({" ).append( NEWLINE );

    addJsProperty( out, "type", JsonUtils.toJsString( className ), INDENT1, true );
    addJsProperty( out, "name", JsonUtils.toJsString( id ), INDENT1, false );

    // Render definitions
    for ( String definitionName : compType.getDefinitionNames() ) {
      addCommaAndLineSep( out );
      this.writeDefinition( definitionName, out, context, comp, compType );
    }

    out.append( NEWLINE ).append( "});" ).append( NEWLINE );
  }

  private void writeDefinition(
      String definitionName,
      StringBuilder out,
      CdfRunJsDashboardWriteContext context,
      GenericComponent comp,
      GenericComponentType compType ) throws ThingWriteException {
    String indent = INDENT1;

    boolean isDefaultDefinition = StringUtils.isEmpty( definitionName );
    if ( !isDefaultDefinition ) {
      addJsProperty( out, definitionName, " {", INDENT1, true );
      indent = INDENT2;
    }

    CdfRunJsDashboardWriteContext childContext = context.withIndent( indent );
    childContext.setIsFirstInList( true );

    IThingWriterFactory factory = context.getFactory();
    for ( PropertyTypeUsage propUsage : compType.getPropertiesByDefinition( definitionName ) ) {
      String propName = propUsage.getName();
      // The 'name' property is handled specially
      if ( !( isDefaultDefinition && "name".equalsIgnoreCase( propName ) ) ) {
        PropertyBinding propBind = comp.tryGetPropertyBindingByName( propName );
        if ( propBind != null ) {
          IThingWriter writer;
          try {
            writer = factory.getWriter( propBind );
          } catch ( UnsupportedThingException ex ) {
            throw new ThingWriteException( ex );
          }

          // TODO: empty properties are not output
          // and the NEWLINE is already output...
          if ( !isDefaultDefinition && childContext.isFirstInList() ) {
            out.append( NEWLINE );
          }
/*
          if ( propBind.getAlias().equalsIgnoreCase( "datasource" ) ) {
            try {
              propBind = new UnresolvedPropertyBinding.Builder()
                .setInputType( propBind.getInputType() )
                .setAlias( "datasourceName" )
                .setValue( propBind.getValue() ).build( comp, ( new MetaModel.Builder() ).build() );
            } catch ( ValidationException e ) {
              e.printStackTrace();
            }
          }
*/
          writer.write( out, childContext, propBind );
        }
      }
    }

    if ( comp.getExtensionPropertyBindingCount() > 0 ) {
      // HACK: CCC V1 properties have to go into the "chartDefinition" definition...
      boolean isCCC = compType.getName().startsWith( "ccc" );
      if ( isCCC ? !isDefaultDefinition : isDefaultDefinition ) {
        Iterable<ExtensionPropertyBinding> propBinds = comp.getExtensionPropertyBindings();
        for ( ExtensionPropertyBinding propBind : propBinds ) {
          IThingWriter writer;
          try {
            writer = factory.getWriter( propBind );
          } catch ( UnsupportedThingException ex ) {
            throw new ThingWriteException( ex );
          }

          if ( !isDefaultDefinition && childContext.isFirstInList() ) {
            out.append( NEWLINE );
          }

          writer.write( out, childContext, propBind );
        }
      }
    }

    if ( !isDefaultDefinition ) {
      out.append( NEWLINE ).append( INDENT1 ).append( "}" );
    }
  }

}
