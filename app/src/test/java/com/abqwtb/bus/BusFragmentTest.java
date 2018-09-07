package com.abqwtb.bus;

import android.support.v4.app.Fragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment;

@RunWith(RobolectricTestRunner.class)
public class BusFragmentTest {

  private Fragment fragment;

  @Before
  public void setUp() {
    fragment = BusFragment.newInstance(746);
  }

  @Test
  public void testUpdateDisplay() {
    startFragment(fragment);
    assertNotNull(fragment);
    fragment.
  }


}