package wenjalan.starbot.engine.audio;

import net.dv8tion.jda.api.audio.*;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// repeats whatever a certain user says
public class MegaphoneHandler implements AudioReceiveHandler, AudioSendHandler {

    // the bytes to repeat
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

    // the user to megaphone for
    private final long userId;

    // the volume to set the megaphone to
    private final float volume;

    // constructor
    public MegaphoneHandler(long userId, float volume) {
        this.userId = userId;
        this.volume = volume;
    }

    // send handler events
    @Override
    public boolean canProvide() {
        return !queue.isEmpty();
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        byte[] nextFrame = queue.poll();
        return nextFrame == null ? null : ByteBuffer.wrap(nextFrame);
    }

    @Override
    public boolean isOpus() {
        return false;
    }

    // receive handler events
    @Override
    public boolean canReceiveCombined() {
        return false;
    }

    @Override
    public boolean canReceiveUser() {
        return queue.size() < 10;
    }

    @Override
    public boolean canReceiveEncoded() {
        return false;
    }

    @Override
    public void handleEncodedAudio(@Nonnull OpusPacket packet) {
        return;
    }

    @Override
    public void handleCombinedAudio(@Nonnull CombinedAudio combinedAudio) {
        return;
    }

    @Override
    public void handleUserAudio(@Nonnull UserAudio userAudio) {
        if (userAudio.getUser().getIdLong() != userId) {
            return;
        }
        byte[] data = userAudio.getAudioData(this.volume);
        queue.add(data);
    }

    @Override
    public boolean includeUserInCombinedAudio(@Nonnull User user) {
        return false;
    }

    public long getUserId() {
        return userId;
    }
}
