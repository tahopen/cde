/*!
 * Copyright 2018-2021 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.cache.impl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.webdetails.cdf.dd.DashboardCacheKey;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CacheTest {
  private static final String EHCACHE_FILE_PATH = "src/test/resources/ehcache.xml";
  private static IReadAccess mockedReadAccess;
  private static IContentAccessFactory contentAccessFactory;
  private static Cache cache;
  private DashboardCacheKey key;
  private CdfRunJsDashboardWriteResult value;

  @BeforeClass
  public static void beforeAll() throws Exception {
    mockedReadAccess = mock( IReadAccess.class );
    when( mockedReadAccess.getFileInputStream( any() ) )
      .thenReturn( new FileInputStream( EHCACHE_FILE_PATH.replace( "/", File.separator ) ) );
    contentAccessFactory = mock( IContentAccessFactory.class );
    when( contentAccessFactory.getPluginSystemReader( anyString() ) ).thenReturn( mockedReadAccess );
    cache = new Cache( contentAccessFactory );
  }

  @AfterClass
  public static void afterAll() {
    cache = null;
    contentAccessFactory = null;
    mockedReadAccess = null;
  }

  @Before
  public void setUp() {
    cache.removeAll();
    key = new DashboardCacheKey("mock", "", false, false, "", "" );
    value = new CdfRunJsDashboardWriteResult.Builder().build();
  }

  @After
  public void tearDown() {
    value = null;
    key = null;
  }

  @Test( expected = InitializationException.class )
  public void testInitializationFailLoadConfiguration() throws Exception {
    when( mockedReadAccess.getFileInputStream( any() ) )
      .thenThrow( new IOException( "mocked IOException" ) );
    new Cache( contentAccessFactory );
    fail( "InitializationException not thrown" );
  }

  @Test( expected = InitializationException.class )
  public void testInitializationFailLoadCacheManager() throws Exception {
    when( mockedReadAccess.getFileInputStream( anyString() ) )
      .thenReturn( null );
    new Cache( contentAccessFactory );
    fail( "InitializationException not thrown" );
  }

  @Test
  public void testPut() {
    cache.put( key, value );
    assertEquals( value, cache.get( key ) );
  }

  @Test
  public void testGet() {
    cache.put( key, value );
    assertEquals( value, cache.get( key ) );
  }

  @Test
  public void testGetKeys() {
    cache.put( key, value );
    assertEquals( 1, cache.getKeys().size() );
  }

  @Test
  public void testRemove() {
    cache.put( key, value );
    assertEquals( value, cache.get( key ) );
    cache.remove( key );
    assertNull( cache.get( key ) );
  }

  @Test
  public void testRemoveAll() {
    cache.put( key, value );
    assertEquals( 1, cache.getKeys().size() );
    cache.removeAll();
    assertEquals( 0, cache.getKeys().size() );
  }
}
