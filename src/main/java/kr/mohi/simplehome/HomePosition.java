package kr.mohi.simplehome

import java.io.Serializable;

import cn.nukkit.level.Level;
import cn.nukkit.level.Position;

public class HomePosition implements Serializable{
	private double x, y, z;
	private String name, owner;
	private Level level;
	
	public HomePosition(double x, double y, double z, Level level, String name, String owner) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.level = level;
		this.name = name;
		this.owner = owner;
	}
	
	public Position toPosition() {
		return new Position(this.x, this.y, this.z, this.level);
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}
	
	public double getZ() {
		return this.z;
	}
	
	public Level getLevel() {
		return this.level;
	}
	
	public String getOwner() {
		return this.owner;
	}
	
	public String getName() {
		return this.name;
	}
}
