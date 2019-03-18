package ThreeDimensionalComponents;

public final class Globals {
	public static class Camera {
		
		//PURPOSE OF STRICTCLIP:
		//If any points pass behind the camera, they cause the entire shape
		//they are attached to not be rendered
		public static final boolean strictClip = true;
	}
	public static class Numbers {
		
		//PURPOSE OF INFINITY
		//Single overflow calculation
		public static final double infinity = 1.0/0.0;
	}
}
