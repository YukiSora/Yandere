package moe.yukisora.yandere.interfaces;

import java.io.Serializable;
import java.util.List;

import moe.yukisora.yandere.modles.ImageData;
import retrofit2.Call;

public interface CallGenerator extends Serializable {
    Call<List<ImageData>> generateCall(int page);
}
