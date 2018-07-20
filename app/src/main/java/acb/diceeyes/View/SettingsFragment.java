package acb.diceeyes.View;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import acb.diceeyes.R;
import acb.diceeyes.Storage;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private Spinner aliasSpinner;
    private Storage storage;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        aliasSpinner = (Spinner) view.findViewById(R.id.aliasContentSpinner);
        aliasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                try {
                    storeAlias(aliasSpinner.getSelectedItem().toString());
                } catch (Exception e){
                    Toast.makeText(getActivity(), R.string.error_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                return;
            }

        });

        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        storage = ((MainActivity)this.getActivity()).getStorage();
    }

    /**
     * Stores a pseudonym of the user for identifying him and naming the photos after him
     */
    private void storeAlias(String input) {
        try {

            //get index of spinner
            int index = 0;
            for (int i=0;i<aliasSpinner.getCount();i++){
                if (aliasSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(input)){
                    index = i;
                    break;
                }
            }
            storage.setAlias(getActivity().getApplicationContext(), input, index);

        } catch (NullPointerException e) {
            Log.d(TAG, "Alias not stored.");
        }

    }

}
