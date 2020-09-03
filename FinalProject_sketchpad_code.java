import java.awt.*;  import java.awt.event.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;


class sketch4 extends Frame {
class obj {
 int xi,yi,xj,yj,tp,sl=0;
 boolean isDragging = false;
 obj() { }
 obj(int a, int b, int c, int d, int t) {
  xi=a; yi=b; xj=c; yj=d; tp=t; }
 class ipair { 
  int x,y;
  ipair(int xx, int yy) { x=xx; y=yy; }
 }
 ipair  add(ipair U, ipair W) { return new ipair(U.x+W.x, U.y+W.y); }
 ipair  sub(ipair U, ipair W) { return new ipair(U.x-W.x, U.y-W.y); }
 ipair scale(ipair U, float s) { return new ipair((int)(s*(float)U.x), (int)(s*(float)U.y)); }
 int dist(ipair P, ipair Q) { return (int)Math.sqrt((P.x-Q.x)*(P.x-Q.x) + (P.y-Q.y)*(P.y-Q.y)); }
 int  dot(ipair P, ipair Q) { return P.x*Q.x + P.y*Q.y; }
 int segdist(int xp,int yp) { // distance from point (xp,yp) to line segment (xi,yi,xj,yj)
  ipair I=new ipair(xi,yi), J=new ipair(xj,yj), P=new ipair(xp,yp), V,N;
  V = sub(J,I);             // V is the vector from I to J
  int k = dot(V, sub(P,I)); // k is the non-normalized projection from P-I to V
  int L2= dot(V,V);         // L2 is the length of V, squared
  if (k<=0) N = I;          // if the projection is negative, I is nearest (N)
   else if (k>=L2) N = J;   // if the projection too large, J is nearest (N)
   else N = add(I, scale(V,(float)k/(float)L2)); // otherwise, N is scaled onto V by k/L2
   return dist(P,N);
 }
}
ArrayList<obj> objs = new ArrayList<obj>();
Stack<obj> redoStack = new Stack<obj>();

int x0,y0,type,select=0, dmin=9999999;
int offsetx1,offsety1, offsetx2,offsety2;
private Color current;


obj closest = new obj();
private boolean isToDelete = false;
private boolean isToCutImage = false;
obj cutLine = null;
private boolean isToDrag = false;
private Color color; 

 sketch4() {  
  setSize(150,200);
  setLayout(new FlowLayout());  
  Button btn1 = new Button("back");
  Button btn6 = new Button("redo");
  Button btn2 = new Button("lines");
  Button btn14 = new Button("Scribbled Line");
  Button btn3 = new Button("Rectangles");
  Button btn4 = new Button("Find Closest");
  Button btn5 = new Button("Circle");
  Button btn7 = new Button("Drag");
  Button btn8 = new Button("Save");
  Button btn9 = new Button("Load");
  Button btn10 = new Button("Delete");
  Button btn11 = new Button("Cut/Paste");
  Button btn12 = new Button("Group");
  Button btn13 = new Button("Ungroup");
  
  Button btn15 = new Button("Change Color To Red");
  Button btn16 = new Button("Change Color To Green");
  Button btn17 = new Button("Change Color To Pink");
  
  
  
  add(btn1); add(btn2); add(btn3); add(btn4);add(btn5);add(btn6);add(btn7);add(btn8);add(btn9);add(btn10);add(btn11);add(btn12);add(btn13);add(btn14);
  add(btn15);add(btn16);add(btn17);
  btn1.addActionListener( e -> {
    if(objs.size()!= 0) redoStack.push(objs.remove( objs.size()-1 ));
    repaint(); } );
  btn2.addActionListener( e -> type=0 );//line
  btn3.addActionListener( e -> type=1 ); //rectangle
  btn5.addActionListener( e -> type=2 );//Circle
  btn4.addActionListener( e -> { select=1; closest.sl=0; repaint(); } );
  btn6.addActionListener(e -> {
	  if (redoStack.size()!=0) {
		  objs.add(redoStack.pop());
		  repaint();
	  }
  });
  btn7.addActionListener(e -> {
	  isToDrag(true);
  });
  btn8.addActionListener(e->{
	  saveImage(this);
  });
  
  btn9.addActionListener(e->{
	  loadImage(this);
  });
  

  btn10.addActionListener(e->{
	  deleteImage();
  });
  
  btn11.addActionListener(e->{
	  cutImage();
  });
  
  btn15.addActionListener(e->{
	  changeColor(Color.RED);
  });
  
  btn16.addActionListener(e->{
	  changeColor(Color.GREEN);
  });
  
  btn17.addActionListener(e->{
	  changeColor(Color.PINK);
  });
  
  addMouseListener( new MouseAdapter() {
   public void mouseReleased(MouseEvent e){
	   select=0;
	   System.out.println("Images drawn "+objs.size());
	   if (isToDelete) isToDelete = !isToDelete;
	   if (isToDrag) {
		   isToDrag(false);
			closest = null;
	   }
		
	   }
   public void mouseClicked(MouseEvent e){
	   if ( null != cutLine && isToCutImage) {
		   cutLine.xi = e.getX();
		   cutLine.yi = e.getY();
		   objs.add(cutLine);
		   isToCutImage = false;
		   closest = null;
		   cutLine = null;
		   return;
	   }
	   if (isToDelete && e.getClickCount() >1) {
		   objs.forEach( ob -> {
	             ob.sl = 0;
	             int d=ob.segdist(e.getX(),e.getY());
	             if( dmin > d ) { closest=ob; }
	             } );
		   objs.remove(closest);
		   
	   }
	   repaint();
   }
   public void mousePressed(MouseEvent e){ 
     x0 = e.getX(); 
     y0 = e.getY(); 
     if (isToDelete) return;
     if (isToDrag && null != closest) {
    	 
    	 offsetx1 = e.getX() - closest.xi;
    	 offsety1 = e.getY() - closest.yi;
    	 
    	 offsetx2 = e.getX() - closest.xj;
    	 offsety2 = e.getX() - closest.yj;
    	 
     }
     if (!isToDrag)  {
         objs.add( new obj(x0,y0,x0,y0,type) ); }};
     });
  
  addMouseMotionListener(new MouseMotionAdapter() {
    public void mouseMoved(MouseEvent e) {
      if (select==1 && objs.size()!=0) {
          objs.forEach( ob -> {
             ob.sl = 0;
             int d=ob.segdist(e.getX(),e.getY());
             if( dmin > d ) { closest=ob; dmin=d; ob.isDragging = true; }
             } );
      closest.sl=1;
      repaint();
    }   
   
    
    }
    
    
    public void mouseDragged(MouseEvent e) {
    
//    	if (closest.isDragging) {
//      	  System.out.println("Dragging");
//      	  objs.remove(closest);
//      	  
//      	  closest.xi = closest.xi-offsetx;
//      	  closest.yi = closest.yi-offsety;
//      	  closest.xj = closest.xj-offsetx;
//      	  closest.yj = closest.yj-offsety;
//      	  
//      	  objs.add(new obj(closest.xi,closest.yi,e.getX()+closest.xj,e.getY()+closest.yi,type));
//      	  offsetx = e.getX();
//      	  offsety = e.getY();
//      	  repaint();
//      	  return;
//      	  
//        }
    	
    	if (isToDrag && null != closest) {
    		
    		closest.xi = offsetx1 + e.getX();
    		closest.yi = offsety1 + e.getY();
    		closest.xj = offsetx2 + e.getX();
    		closest.yj = offsety2 + e.getY();
    		
    		
    		repaint();
    	}
    	else if (isToDelete) return;
    	else if(!isToDrag){
    		objs.remove( objs.size()-1 );
    	      objs.add(new obj(x0,y0,e.getX(),e.getY(),type));
    	      repaint();
    	}
      
      }} );
 }
private void changeColor(Color color) {
	this.color = color;
}
private void isToDrag(boolean flag) {
	this.isToDrag  = flag;
}
private void cutImage() {
	this.isToCutImage  = true;
	if (null != closest && isToCutImage) {
		cutLine = closest;
		objs.remove(closest);
		closest = null;
		repaint();
	}
}
private void deleteImage() {
	this.isToDelete  = true;
}
private void loadImage(Frame parentFrame) {
	FileDialog fd;
	   
	   fd = new FileDialog(parentFrame, "Save to File", FileDialog.LOAD);
	   fd.show();
	   
	   String fileName = fd.getFile(); 
	   
	   if (fileName == null)
	      return;  // User has canceled.
	      
	   String directoryName = fd.getDirectory();
	   
	   File file = new File(directoryName, fileName); 

	FileInputStream f = null;
	try {
		f = new FileInputStream(file);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	DataInputStream in = new DataInputStream(f);
	BufferedReader br = new BufferedReader(new java.io.InputStreamReader(in));
	String str;
	objs = new ArrayList<obj>();
	
	int count=0;
	
	try {
		while ((str = br.readLine())!=null) {
			String s[] = str.split("\\s");
			if (count==0) {
				count++;
				continue;
			}
			obj line = new obj();
			objs.add(line);
			
			int xi = Integer.parseInt(s[1]);
			int yi = Integer.parseInt(s[2]);
			
			int xj = Integer.parseInt(s[3]);
			int yj = Integer.parseInt(s[4]);
			int tp = Integer.parseInt(s[0]);
			
			objs.add(new obj(xi,yi,xj,yj,tp));
			count++;
		}
	} catch (NumberFormatException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	repaint();
}

int max(int a,int b){return a>b ? a:b;}
int min(int a,int b){return a>b ? b:a;}
int abs(int a){return a>0 ? a : -a;}


public void saveImage(Frame parentFrame) {
	FileDialog fd;
	   
	   fd = new FileDialog(parentFrame, "Save to File", FileDialog.SAVE);
	   fd.show();
	   
	   String fileName = fd.getFile(); 
	   
	   if (fileName == null)
	      return;  // User has canceled.
	      
	   String directoryName = fd.getDirectory();
	   
	   File file = new File(directoryName, fileName);  
	   
	   PrintWriter out;  
	   try { 
	      out = new PrintWriter( new FileWriter(file) );
	   }
	   catch (IOException e) {
	      return;
	   }
	   
	   out.println(objs.size()); 
	   
	   for (int i = 0; i < objs.size(); i++) {
	 	  out.print(objs.get(i).tp);
	   	  out.print(" ");
	      out.print(objs.get(i).xi);
	      out.print(" ");
	      out.print(objs.get(i).yi);
	      out.print(" ");
	      out.print(objs.get(i).xj);
	      out.print(" ");
	      out.print(objs.get(i).yj);
	      out.println();
	   }
	   out.close();
}

public void paint(Graphics g){
	g.setColor(color);
   dmin=9999999;
   objs.forEach( ob -> {
   if (ob.sl==1) g.setColor(Color.RED);
   if (ob.tp==0) g.drawLine(ob.xi,ob.yi,ob.xj,ob.yj);
   else if(ob.tp==2) g.drawOval(ob.xi, ob.yi, ob.xj, ob.yj);
     else g.drawRect(min(ob.xi,ob.xj),min(ob.yi,ob.yj),
                     abs(ob.xi-ob.xj),abs(ob.yi-ob.yj));
   if (ob.sl==1) g.setColor(Color.BLACK); } ); }

public static void main(String[] args){ new sketch4().setVisible(true); }  }

// "/cygdrive/c/Program Files/Java/jdk-11.0.1/bin/javac" sketch4.java
// java sketch4