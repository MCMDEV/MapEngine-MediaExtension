package de.pianoman911.mapengine.media.movingimages;

import de.pianoman911.mapengine.api.drawing.IDrawingSpace;
import de.pianoman911.mapengine.api.util.FullSpacedColorBuffer;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovingImagePlayer {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private final FrameSource source;
    private final int bufferSize;
    private final IDrawingSpace drawingSpace;
    private final Queue<FullSpacedColorBuffer> buffer;
    private boolean running;
    private boolean bufferBuilt;
    private boolean ended;

    public MovingImagePlayer(FrameSource source, int bufferSize, IDrawingSpace drawingSpace) {
        this.source = source;
        this.bufferSize = bufferSize;
        this.drawingSpace = drawingSpace;
        this.buffer = new ArrayDeque<>(bufferSize);

        EXECUTOR.execute(() -> {
            while (!Thread.interrupted()) {
                if (running && !ended) {
                    FullSpacedColorBuffer frame = source.next();
                    if (frame != null) {
                        buffer.add(frame);
                    }   else   {
                        ended = true;
                        return;
                    }

                    if (bufferBuilt || buffer.size() >= bufferSize) {
                        bufferBuilt = true;
                        if (!buffer.isEmpty()) {
                            FullSpacedColorBuffer current = buffer.poll();
                            drawingSpace.buffer(current, 0, 0);
                            drawingSpace.flush();

                        }
                    }
                }
            }
        });
    }

    public FrameSource source() {
        return source;
    }

    public int bufferSize() {
        return bufferSize;
    }

    public IDrawingSpace drawingSpace() {
        return drawingSpace;
    }

    public Queue<FullSpacedColorBuffer> buffer() {
        return buffer;
    }

    public boolean running() {
        return running;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public boolean ended() {
        return ended;
    }

    public void restart() {
        if (running) stop();
        start();
    }
}
