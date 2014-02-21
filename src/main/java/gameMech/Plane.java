package gameMech;

import javafx.util.Pair;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * User: maxim
 * Date: 30.11.13
 * Time: 4:56
 */

//TODO:передавать размеры экрана,вычислять defaul position

public class Plane {
	Direction direction;
	Position position;
	Boolean left;
	double rotation = 0.0;
	//Integer health;
	//Integer damage;
	//тригонометрическо-округляющая магия
	public static final MathContext myMathContext = new MathContext(7, RoundingMode.HALF_UP);
	public Plane(Boolean left/*, Integer damage*/){
		//this.damage = damage;
		this.left = left;
		if (left){
			position = new Position(250.0, 48.0);
		} else {
			position = new Position(850.0, 50.0);
		}
		direction = new Direction(0.0, 0.0);
		rotation = 13;
	}

	public void move(){
		position.move(direction);
	}

	public void changeDirection(double x, double y){
		//System.out.println("changeDirection(double x, double y) x= " + Math.round(x) + " y= " + Math.round(y));
//		double rotate;
//		if (left){
//			rotate = GameMech.findAngle(1.0, 0.0, direction.getX(), direction.getY());
//		} else{
//			rotate = GameMech.findAngle(-1.0, 0.0, direction.getX(), direction.getY());
//		}

		/*
		if (!(direction.getX() == 0  &&  direction.getY()== 0  &&  x == 0)){
			double  oldX = direction.getX(),
					oldY = direction.getY(),
					length = (new BigDecimal(Math.sqrt(oldX*oldX + oldY*oldY), myMathContext)).setScale(5, RoundingMode.HALF_UP).doubleValue(),
					newX, newY;
			if (oldX != 0  || oldY !=0){
				if (oldX >= 0){
					newX = oldX + x*oldX/length;
					newX = (Math.round(newX)>0) ? newX : 0;
				} else {
					newX = oldX - x*oldX/length;
					newX = (Math.round(newX)<0) ? newX : 0;
				}

				if (oldY >= 0){
					newY = oldY + x*oldY/length;
					newY = (newY>0) ? newY : 0;
				} else {
					newY = oldY - x*oldY/length;
					newY = (newY<0) ? newY : 0;
				}
			} else {
				//if (left){
					//newX = (x > 0) ? x : 0.0;
				newX = (new BigDecimal(Math.cos(x)*GameMech.getAbsoluteVector(getRotation()).getX(), new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue();
				//} else {
					//newX = (x < 0) ? x : 0.0;
				//}
				newY = (new BigDecimal(Math.sin(x)*GameMech.getAbsoluteVector(getRotation()).getY(), new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue();
			}
			//newX = (oldX != 0  || oldY !=0)  ?  ((oldX >= 0)? oldX + x*oldX/length : oldX - x*oldX/length)  :  ((left)?1.0:-1.0);


			//newY = (oldX != 0  || oldY !=0)  ?  ((oldY >= 0)? oldY + x*oldY/length : oldY - x*oldY/length)  :  0;

			Direction newDirection = new Direction(newX, newY);
			if (this.left){
				newDirection = GameMech.getVector(newDirection.getX(), newDirection.getY(), y*4, true);
			} else {
				newDirection = GameMech.getVector(newDirection.getX(), newDirection.getY(), y*4, false);
			}

			//System.out.println("new Direction: " + newDirection);
			direction = newDirection;
		} */


		//для левого самолетика координаты стандартные, для правого x в другую сторону направлен
		if (x != 0 || y!=0){
				double newX = direction.getX(), newY = direction.getY();
				if (x != 0){
					//стоит на месте
					if (newX == 0 && newY == 0){
						newX = (new BigDecimal(Math.cos(Math.toRadians(rotation)), new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue();
						if (left)
							newY = (new BigDecimal(Math.sin(Math.toRadians(rotation)), new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue();
						else
							newY = -(new BigDecimal(Math.sin(Math.toRadians(rotation)), new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue();

						//если стоим, то левый не может двигаться назад, равно как и правый
						if ((x >= 0 && left==true) || (x <= 0 && left==false)){
							newX = (new BigDecimal(x*newX, new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue();
							newY = (new BigDecimal(x*newY, new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue();
						} else {
							newX = 0.0;
							newY = 0.0;
						}

					} else {
						/*newX = (new BigDecimal(  modulSum(direction.getX(), x*Math.cos(Math.toRadians(rotation))), new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue(); 	//TODO сделать нормальное округление
						newY = (new BigDecimal(  modulSum(direction.getY(), x*Math.sin(Math.toRadians(rotation))), new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue();    //TODO сделать нормальное округление
						*/
						Direction newDirection = modulSum(direction, x);
						newX = newDirection.getX();
						newY = newDirection.getY();
					}
				}
				if (y != 0){
					rotation += y;
					//TODO: проверить, правильно сработало?
					if (newX != 0 || newY != 0){
						Direction direction1 = GameMech.getVector(newX, newY, y, left);
						newX = direction1.getX();
						newY = direction1.getY();
					}
				}
				System.out.println("newX: " + newX + " newY: " + newY);
				direction.setDirection(newX, newY);
		}
	}

	//TODO: по-другому назвать
	public Direction modulSum(Direction direction, double delta/*double direction, double delta)*/){
		//если было отрицательное направление,  и увеличиваем, то больше нуля не станет и наоборот
		//справедливо для левого,
		double newX = direction.getX(),
				newY = direction.getY();
		/*if (
			(  (direction.getX() + delta*Math.cos(Math.toRadians(rotation))) >= 0 && direction.getX() > 0    &&   (direction.getY() + delta*Math.sin(Math.toRadians(rotation))) >=0 && direction.getY() > 0  )
		||  (  (direction.getX() + delta*Math.cos(Math.toRadians(rotation))) <= 0 && direction.getX() < 0    &&   (direction.getY() + delta*Math.sin(Math.toRadians(rotation))) <=0 && direction.getY() < 0  )
		   )
		{
			newX += delta*Math.cos(Math.toRadians(rotation));
			newY += delta*Math.sin(Math.toRadians(rotation));

			return new Direction(newX, newY);
		} else {
			return new Direction(0.0, 0.0);
		}  */

		double deltaX;

	   	if (left){
			if (newX < 0){
				deltaX = delta * Math.cos(Math.toRadians(rotation));
			} else {
				deltaX = delta * Math.cos(Math.toRadians(rotation));
			}
		} else {
			if (Math.cos(Math.toRadians(rotation)) < 0){
				deltaX = delta * Math.cos(Math.toRadians(rotation));
			} else {
				deltaX = delta * Math.cos(Math.toRadians(rotation));
			}
		}


		//double deltaX = (left) ? (newX < 0) ? delta * Math.cos(Math.toRadians(rotation)) : delta * Math.cos(Math.toRadians(rotation)) : (Math.cos(Math.toRadians(rotation)) < 0) ? delta * Math.cos(Math.toRadians(rotation)) : delta * Math.cos(Math.toRadians(rotation)); //с модулем - костыль.
		//на самом деле, rotation правого самолета находится на единичной окружности напротив того положения, в котором мы думаем
		//поэтому, если он летит в левую полуплоскость, его cos(rotation)>0. Если он начинает лететь в правую полуплоскость, то s(rotation)<0
		// и когда нажимается <- скорость будет гаситься.
		//для левого самолета все волшебно и живописно - когда летит в правую полуплоскость, cos>0, если ->   => направление по x изменяется правильно
		// когда летит в левую полуплоскость, cos<0, если ->    => направление по x тоже изменяется правильно

		//итого, поэтому стоит модуль для правого
		double deltaY;// = (left) ? (newY < 0) ? -delta * Math.sin(Math.toRadians(rotation)) : delta * Math.sin(Math.toRadians(rotation)) : (newY > 0)?-delta * Math.sin(Math.toRadians(rotation)):delta * Math.sin(Math.toRadians(rotation));
		if (left){
			deltaY = delta * Math.sin(Math.toRadians(rotation));
		} else {
			if (newY > 0){
				deltaY = -delta * Math.sin(Math.toRadians(rotation));
			} else {

				deltaY = -delta * Math.sin(Math.toRadians(rotation));
			}
		}

		System.out.println("rotation: " + rotation);
		if (	/*	//TODO: direction.getX() + deltaX == 0 что тогда? м?
				getScaled(direction.getX() + deltaX) >= 0 && getScaled(direction.getX()) < 0
			||  getScaled(direction.getY() + deltaY) >= 0 && getScaled(direction.getY()) < 0

			||	getScaled(direction.getX() + deltaX) <= 0 && getScaled(direction.getX()) > 0
			||  getScaled(direction.getY() + deltaY) <= 0 && getScaled(direction.getY()) > 0
				*/
				getScaled(getDirectionLength()) != 0.0 && getScaled(getDirectionLength() + ((left) ? delta : -delta), 4) <= 0
		   ){
			return new Direction(0.0, 0.0);
		} else{
			return new Direction(getScaled(direction.getX()+deltaX), getScaled(direction.getY()+deltaY));
		}

	}

	public static double getScaled(double input){
		return (new BigDecimal(  input  , new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue();
	}

	public static double getScaled(double input, int scale){
		return (new BigDecimal(  input  , new MathContext(7, RoundingMode.HALF_UP))).setScale(scale, RoundingMode.HALF_UP).doubleValue();
	}

	public Direction getDirection(){
		return direction;
	}

	public void setDirection(Direction direction){
		this.direction = direction;
		try {
			double nulDirectionX = (left)?1:-1;
			this.rotation = GameMech.findAngle(nulDirectionX, 0, direction.getX(), direction.getY());
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void setDirection(Direction direction, double rotation){
		this.direction = direction;
		try {
			double nulDirectionX = (left)?1:-1;
			this.rotation = rotation;
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public Position getPosition(){
		return position;
	}

	public double getDirectionLength(){
		double result = (new BigDecimal( Math.sqrt(direction.getX()*direction.getX() + direction.getY()*direction.getY() ) , new MathContext(7, RoundingMode.HALF_UP))).setScale(5, RoundingMode.HALF_UP).doubleValue();
		return result;
	}

	@Override
	public String toString() {
		return "Plane{" +
				"direction=" + direction +
				", position=" + position +
				", left=" + left +
				'}';
	}

	public double getRotation(){
		//тут пересчет в rotation
		//надо добавить класс,
		// от которого будет наследоваться plane,
		// чтобы пули тоже так же обсчитывались
		/*double d = 0.0;  //TODO: Fix GAVNO - неправильно выдается угол, отображается правильно, но отрицательный
		if (left){
			try {
				d = GameMech.findAngle(1.0, 0.0, direction.getX(), direction.getY());
			} catch (Exception e){
				e.printStackTrace();
			}
		} else{
			try {
				d = GameMech.findAngle(-1.0, 0.0, direction.getX(), direction.getY());
			} catch (CalculateNullVectorAngleException e) {
				e.printStackTrace();
			}
		}
		return d; */
		return rotation;
	}

	public Boolean getLeft(){
		return left;
	}

	//	public void damage(Integer minusHealth){
//		health -= minusHealth;
//	}
}
