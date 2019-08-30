/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.distributed.internal.membership;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.apache.geode.distributed.internal.ClusterDistributionManager;
import org.apache.geode.distributed.internal.membership.adapter.GMSMemberAdapter;
import org.apache.geode.distributed.internal.membership.gms.GMSMember;
import org.apache.geode.distributed.internal.membership.gms.GMSMembershipView;
import org.apache.geode.internal.net.SocketCreator;
import org.apache.geode.test.junit.categories.MembershipTest;

@Category({MembershipTest.class})
public class MembershipViewJUnitTest {

  private List<GMSMember> members;

  @Before
  public void initMembers() throws Exception {
    int numMembers = 10;
    members = new ArrayList<>(numMembers);
    for (int i = 0; i < numMembers; i++) {
      members.add(
          ((GMSMemberAdapter) new InternalDistributedMember(SocketCreator.getLocalHost(), 1000 + i)
              .getNetMember()).getGmsMember());
    }
    // view creator is a locator
    members.get(0).setVmKind(ClusterDistributionManager.LOCATOR_DM_TYPE);
    members.get(0).setVmViewId(0);
    members.get(0).setPreferredForCoordinator(true);

    // members who joined in view #1
    for (int i = 1; i < (numMembers - 1); i++) {
      members.get(i).setVmViewId(1);
      members.get(i).setVmKind(ClusterDistributionManager.NORMAL_DM_TYPE);
      members.get(i).setPreferredForCoordinator(false);
    }

    // member joining in this view
    members.get(numMembers - 1).setVmViewId(2);
    members.get(numMembers - 1).setVmKind(ClusterDistributionManager.NORMAL_DM_TYPE);
  }

  private void setFailureDetectionPorts(GMSMembershipView view) {
    int numMembers = members.size();
    // use the membership port as the FD port so it's easy to figure out problems
    for (int i = 0; i < numMembers; i++) {
      view.setFailureDetectionPort(members.get(i), members.get(i).getPort());
    }
  }

  @Test
  public void testCreateView() throws Exception {
    int numMembers = members.size();
    GMSMembershipView view = new GMSMembershipView(members.get(0), 2, members);
    setFailureDetectionPorts(view);

    assertTrue(view.getCreator().equals(members.get(0)));
    assertEquals(2, view.getViewId());
    assertEquals(members, view.getMembers());
    assertEquals(0, view.getCrashedMembers().size());
    assertEquals(members.get(1), view.getLeadMember()); // a locator can't be lead member
    assertEquals(0, view.getShutdownMembers().size());
    assertEquals(1, view.getNewMembers().size());
    assertEquals(members.get(numMembers - 1), view.getNewMembers().iterator().next());
    assertEquals(members.get(0), view.getCoordinator());

    for (int i = 0; i < numMembers; i++) {
      GMSMember mbr = members.get(i);
      assertEquals(mbr.getPort(), view.getFailureDetectionPort(mbr));
    }

    assertFalse(view.shouldBeCoordinator(members.get(1)));
    assertTrue(view.shouldBeCoordinator(members.get(0)));
    assertEquals(members.get(numMembers - 1),
        view.getCoordinator(Collections.singletonList(members.get(0))));
    members.get(numMembers - 1).setPreferredForCoordinator(false);
    assertEquals(members.get(1), view.getCoordinator(Collections.singletonList(members.get(0))));

    members.get(numMembers - 1).setPreferredForCoordinator(true);
    List<GMSMember> preferred = view.getPreferredCoordinators(
        Collections.<GMSMember>singleton(members.get(1)), members.get(0), 2);
    assertEquals(2, preferred.size());
    assertEquals(members.get(numMembers - 1), preferred.get(0));
  }

  @Test
  public void testRemoveMembers() throws Exception {
    int numMembers = members.size();
    GMSMembershipView view = new GMSMembershipView(members.get(0), 2, new ArrayList<>(members));
    setFailureDetectionPorts(view);

    for (int i = 1; i < numMembers; i += 2) {
      view.remove(members.get(i));
      assertFalse(view.contains(members.get(i)));
    }

    List<GMSMember> remainingMembers = view.getMembers();
    int num = remainingMembers.size();
    for (int i = 0; i < num; i++) {
      GMSMember mbr = remainingMembers.get(i);
      assertEquals(mbr.getPort(), view.getFailureDetectionPort((GMSMember) mbr));
    }
  }

  @Test
  public void testRemoveAll() throws Exception {
    int numMembers = members.size();
    GMSMembershipView view = new GMSMembershipView(members.get(0), 2, new ArrayList<>(members));
    setFailureDetectionPorts(view);

    Collection<GMSMember> removals = new ArrayList<>(numMembers / 2);
    for (int i = 1; i < numMembers; i += 2) {
      removals.add(members.get(i));
    }

    view.removeAll(removals);
    for (GMSMember mbr : removals) {
      assertFalse(view.contains(mbr));
    }
    assertEquals(numMembers - removals.size(), view.size());

    List<GMSMember> remainingMembers = view.getMembers();
    int num = remainingMembers.size();
    for (int i = 0; i < num; i++) {
      GMSMember mbr = remainingMembers.get(i);
      assertEquals(mbr.getPort(), view.getFailureDetectionPort((GMSMember) mbr));
    }
  }

  @Test
  public void testCopyView() throws Exception {
    GMSMembershipView view = new GMSMembershipView(members.get(0), 2, new ArrayList<>(members));
    setFailureDetectionPorts(view);

    GMSMembershipView newView = new GMSMembershipView(view, 3);

    assertTrue(newView.getCreator().equals(members.get(0)));
    assertEquals(3, newView.getViewId());
    assertEquals(members, newView.getMembers());
    assertEquals(0, newView.getCrashedMembers().size());
    assertEquals(members.get(1), newView.getLeadMember()); // a locator can't be lead member
    assertEquals(0, newView.getShutdownMembers().size());
    assertEquals(0, newView.getNewMembers().size());
    assertTrue(newView.equals(view));
    assertTrue(view.equals(newView));
    newView.remove(members.get(1));
    assertFalse(newView.equals(view));
  }

  @Test
  public void testAddLotsOfMembers() throws Exception {
    GMSMembershipView view = new GMSMembershipView(members.get(0), 2, new ArrayList<>(members));
    setFailureDetectionPorts(view);

    GMSMembershipView copy = new GMSMembershipView(view, 2);

    int oldSize = view.size();
    for (int i = 0; i < 100; i++) {
      GMSMember mbr =
          ((GMSMemberAdapter) new InternalDistributedMember(SocketCreator.getLocalHost(), 2000 + i)
              .getNetMember()).getGmsMember();
      mbr.setVmKind(ClusterDistributionManager.NORMAL_DM_TYPE);
      mbr.setVmViewId(2);
      view.add(mbr);
      view.setFailureDetectionPort(mbr, 2000 + i);
    }

    assertEquals(oldSize + 100, view.size());
    for (GMSMember mbr : view.getMembers()) {
      assertEquals(mbr.getPort(), view.getFailureDetectionPort(mbr));
    }

    assertEquals(100, view.getNewMembers(copy).size());
  }

  @Test
  public void testNullPublicKeysNotRetained() throws Exception {
    GMSMembershipView view = new GMSMembershipView(members.get(0), 2, new ArrayList<>(members));
    setFailureDetectionPorts(view);

    GMSMembershipView newView = new GMSMembershipView(view, 3);
    for (GMSMember member : view.getMembers()) {
      view.setPublicKey((GMSMember) member, null);
    }
    newView.setPublicKeys(view);
    for (GMSMember member : view.getMembers()) {
      assertNull(newView.getPublicKey((GMSMember) member));
      assertNull(view.getPublicKey((GMSMember) member));
    }
  }

  /**
   * Test that failed weight calculations are correctly performed. See bug #47342
   */
  @Test
  public void testFailedWeight() throws Exception {
    // in #47342 a new view was created that contained a member that was joining but
    // was no longer reachable. The member was included in the failed-weight and not
    // in the previous view-weight, causing a spurious network partition to be declared
    GMSMember members[] =
        new GMSMember[] {
            ((GMSMemberAdapter) new InternalDistributedMember("localhost", 1).getNetMember())
                .getGmsMember(),
            ((GMSMemberAdapter) new InternalDistributedMember("localhost", 2).getNetMember())
                .getGmsMember(),
            ((GMSMemberAdapter) new InternalDistributedMember("localhost", 3).getNetMember())
                .getGmsMember(),
            ((GMSMemberAdapter) new InternalDistributedMember("localhost", 4).getNetMember())
                .getGmsMember(),
            ((GMSMemberAdapter) new InternalDistributedMember("localhost", 5).getNetMember())
                .getGmsMember(),
            ((GMSMemberAdapter) new InternalDistributedMember("localhost", 6).getNetMember())
                .getGmsMember()};
    int i = 0;
    // weight 3
    members[i].setVmKind(ClusterDistributionManager.LOCATOR_DM_TYPE);
    members[i++].setPreferredForCoordinator(true);
    // weight 3
    members[i].setVmKind(ClusterDistributionManager.LOCATOR_DM_TYPE);
    members[i++].setPreferredForCoordinator(true);
    // weight 15 (cache+leader)
    members[i].setVmKind(ClusterDistributionManager.NORMAL_DM_TYPE);
    members[i++].setPreferredForCoordinator(false);
    // weight 0
    members[i].setVmKind(ClusterDistributionManager.ADMIN_ONLY_DM_TYPE);
    members[i++].setPreferredForCoordinator(false);
    // weight 0
    members[i].setVmKind(ClusterDistributionManager.ADMIN_ONLY_DM_TYPE);
    members[i++].setPreferredForCoordinator(false);
    // weight 10
    members[i].setVmKind(ClusterDistributionManager.NORMAL_DM_TYPE);
    members[i++].setPreferredForCoordinator(false);

    List<GMSMember> vmbrs = new ArrayList<>(members.length);
    for (i = 0; i < members.length; i++) {
      vmbrs.add(members[i]);
    }
    GMSMembershipView lastView =
        new GMSMembershipView((GMSMember) members[0], 4, (List<GMSMember>) (List<?>) vmbrs);
    GMSMember leader = (GMSMember) members[2];
    assertTrue(!leader.preferredForCoordinator());

    GMSMember joiningMember =
        ((GMSMemberAdapter) new InternalDistributedMember("localhost", 7).getNetMember())
            .getGmsMember();
    joiningMember.setVmKind(ClusterDistributionManager.NORMAL_DM_TYPE);
    joiningMember.setPreferredForCoordinator(false);

    // have the joining member and another cache process (weight 10) in the failed members
    // collection and check to make sure that the joining member is not included in failed
    // weight calcs.
    Set<GMSMember> failedMembers = new HashSet<>(3);
    failedMembers.add(joiningMember);
    failedMembers.add(members[members.length - 1]); // cache
    failedMembers.add(members[members.length - 2]); // admin
    List<GMSMember> newMbrs =
        new ArrayList<>(lastView.getGMSMembers());
    newMbrs.removeAll(failedMembers);
    GMSMembershipView newView =
        new GMSMembershipView((GMSMember) members[0], 5, (List<GMSMember>) (List<?>) newMbrs,
            Collections.emptySet(), (Set<GMSMember>) (Set<?>) failedMembers);

    int failedWeight = newView.getCrashedMemberWeight(lastView);
    // System.out.println("last view = " + lastView);
    // System.out.println("failed mbrs = " + failedMembers);
    // System.out.println("failed weight = " + failedWeight);
    assertEquals("failure weight calculation is incorrect", 10, failedWeight);
    Set<GMSMember> actual = newView.getActualCrashedMembers(lastView);
    assertTrue(!actual.contains(members[members.length - 2]));
  }
}