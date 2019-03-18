package base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;


public class BaseFragment extends Fragment{

    public LayoutInflater inflater;
    /**
     *init
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(getContext());
    }
}
