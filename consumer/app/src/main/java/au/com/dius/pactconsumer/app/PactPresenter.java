package au.com.dius.pactconsumer.app;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface PactPresenter<View> {

    void attach(@NonNull View view);

    void detach();

    void onStart(@Nullable Bundle restoreState);

    void onStop(@Nullable Bundle saveState);

}
