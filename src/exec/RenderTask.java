package exec;

import javax.media.opengl.GL2;

public interface RenderTask {
   public void render(GL2 gl2);
   public void init(GL2 gl2);
   public void picking(GL2 gl2, float px, float py);
}
