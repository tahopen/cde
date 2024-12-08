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

package pt.webdetails.cdf.dd.model.inst;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.inst.validation.ComponentUnresolvedPropertyBindingError;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.meta.PropertyTypeUsage;
import pt.webdetails.cdf.dd.util.Utils;

/**
 * A property binding that is not known to exist or not, as a property type usage, in the component type,
 * or as a global property of same name in the MetaModel.
 */
public abstract class UnresolvedPropertyBinding extends PropertyBinding {
  // Never called...
  private UnresolvedPropertyBinding() throws UnsupportedOperationException, ValidationException {
    super( null, null, null );
    throw new UnsupportedOperationException( "Class cannot be instantiated" );
  }

  public static final class Builder extends PropertyBinding.Builder {
    private String _alias;
    private String _inputType;

    public String getAlias() {
      return this._alias;
    }

    public Builder setAlias( String alias ) {
      this._alias = alias;
      return this;
    }

    public String getInputType() {
      return this._inputType;
    }

    public Builder setInputType( String inputType ) {
      this._inputType = inputType;
      return this;
    }

    public PropertyBinding build( Component owner, MetaModel metaModel ) throws ValidationException {
      if ( owner == null ) {
        throw new IllegalArgumentException( "owner" );
      }
      if ( metaModel == null ) {
        throw new IllegalArgumentException( "metaModel" );
      }

      if ( StringUtils.isEmpty( this._alias ) ) {
        throw new ValidationException( new RequiredAttributeError( "Alias" ) );
      }

      ComponentType compType = owner.getMeta();

      // Bind by alias first
      PropertyTypeUsage propUsage = compType.tryGetPropertyUsage( this._alias );
      if ( propUsage == null ) {
        // Only then bind by name
        propUsage = compType.tryGetPropertyUsageByName( this._alias );
      }

      if ( propUsage != null ) {
        ExpectedPropertyBinding.Builder builder = new ExpectedPropertyBinding.Builder();
        builder
          .setPropertyUsage( propUsage )
          .setValue( this.getValue() );

        return builder.build( owner, metaModel );
      }

      // Then try to bind by name to a global property
      PropertyType prop = metaModel.tryGetPropertyType( this._alias );
      if ( prop != null ) {
        ExtensionPropertyBinding.Builder builder = new ExtensionPropertyBinding.Builder();

        // HACK: CCC V1 properties must be made to look like when they were defined
        boolean isCCC = this._alias.startsWith( "ccc" );
        String alias = isCCC ? Utils.toFirstLowerCase( this._alias.substring( 3 ) ) : this._alias;

        builder
          .setAlias( alias )
          .setProperty( prop )
          .setValue( this.getValue() );

        return builder.build( owner, metaModel );
      }

      throw new ValidationException(
        new ComponentUnresolvedPropertyBindingError(
          this._alias,
          owner.getId(),
          owner.getMeta().getLabel() ) );
    }
  }
}
