/**
 * Copyright 2008 code_swarm project team
 *
 * This file is part of code_swarm.
 *
 * code_swarm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * code_swarm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with code_swarm.  If not, see <http://www.gnu.org/licenses/>.
 */

import javax.vecmath.Vector2f;

/**
 * @brief Legacy algorithms describing all physicals interactions between nodes (files and persons)
 * 
 * This is only a rewriting of the initial code_swarm prototype.
 * 
 */
public class PhysicalEngineLegacy extends PhysicalEngine
{
  final private float FORCE_EDGE_MULTIPLIER;
  final private float FORCE_CALCULATION_RANDOMIZER;
  final private float FORCE_TO_SPEED_MULTIPLIER;
  final private float SPEED_TO_POSITION_MULTIPLIER;
  
  /**
   * Constructor for initializing parameters.
   */
  PhysicalEngineLegacy(float forceEdgeMultiplier, float forceCalculationRandomizer, float forceToSpeedMultiplier, float speedToPositionDrag)
  {
    FORCE_EDGE_MULTIPLIER         = forceEdgeMultiplier;
    FORCE_CALCULATION_RANDOMIZER  = forceCalculationRandomizer;
    FORCE_TO_SPEED_MULTIPLIER     = forceToSpeedMultiplier;
    SPEED_TO_POSITION_MULTIPLIER  = speedToPositionDrag;
  }
  
  /**
   * Legacy method that calculate the attractive/repulsive force between a person and one of its file along their link (the edge).
   * 
   * @param edge the link between a person and one of its file 
   * @return force force calculated between those two nodes
   */
  public Vector2f calculateForceAlongAnEdge( code_swarm.Edge edge )
  {
    float distance;
    float fakeForce;
    Vector2f force = new Vector2f();
    
    // distance calculation
    force.set( edge.getNodeTo().getX() - edge.getNodeFrom().getX(), edge.getNodeTo().getY() - edge.getNodeFrom().getY() );
    distance = force.length();
    if (distance > 0) {
      // fake force calculation (increase when distance is different from targeted len")
      fakeForce = (edge.getLen() - distance) / (distance * 3);
      // force ponderation using a re-mapping life from 0-255 scale to 0-1.0 range
      fakeForce = fakeForce * (edge.life * 1.0f) / 255;
      // fake force projection onto x and y axis
      force.scale( fakeForce * FORCE_EDGE_MULTIPLIER );
    }
    
    return force;
  }
  
  /**
   * Legacy method that calculate the repulsive force between two similar nodes (either files or persons).
   * 
   * @param nodeA [in]
   * @param nodeB [in]
   * @return force force calculated between those two nodes
   */
  public Vector2f calculateForceBetweenNodes( code_swarm.Node nodeA, code_swarm.Node nodeB )
  {
    float distx, disty;
    float lensq;
    Vector2f force = new Vector2f();
    
    /** TODO: add comment to this algorithm */
    distx = nodeA.getX() - nodeB.getX();
    disty = nodeA.getY() - nodeB.getY();
    lensq = distx * distx + disty * disty;
    if (lensq == 0) {
      force.set( (float)Math.random()*FORCE_CALCULATION_RANDOMIZER, (float)Math.random()*FORCE_CALCULATION_RANDOMIZER );
    } else if (lensq < 10000) {
      force.set( distx / lensq, disty / lensq );
    }
    
    return force;
  }
  
  /**
   * Legacy method that apply a force to a node, converting acceleration to speed.
   * 
   * @param node [in] Node the node to which the force apply
   * @param force [in] force a force Vector representing the force on a node
   * 
   * TODO: does force should be a property of the node (or not?)
   */
  public void applyForceTo( code_swarm.Node node, Vector2f force )
  {
    float dlen;

    /** TODO: add comment to this algorithm */
    dlen = force.length();
    if ( (dlen > 0) && (node.getMass() > 0)) {
      node.addDX( (force.getX() / (node.getMass() / dlen)) * FORCE_TO_SPEED_MULTIPLIER );
      node.addDY( (force.getY() / (node.getMass() / dlen)) * FORCE_TO_SPEED_MULTIPLIER );
    }
  }

  /**
   * Legacy method that apply a force to a node, converting acceleration to speed.
   * 
   * @param node the node to which the force apply
    */
  public void applySpeedTo( code_swarm.Node node )
  {
    float div;
    // This block enforces a maximum absolute velocity.
    if (node.getSpeed() > node.maxSpeed) {
      Vector2f mag = new Vector2f(node.getDX() / node.maxSpeed, node.getDY() / node.maxSpeed);
      div = mag.length();
      node.mulDX( 1/div );
      node.mulDY( 1/div );
    }
    
    // This block convert Speed to Position
    node.addX( node.getDX() );
    node.addY( node.getDY() );
    
    // Apply drag (reduce Speed for next frame calculation)
    node.mulDX( SPEED_TO_POSITION_MULTIPLIER );
    node.mulDY( SPEED_TO_POSITION_MULTIPLIER );
  }
}

