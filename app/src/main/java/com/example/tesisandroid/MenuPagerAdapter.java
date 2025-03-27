package com.example.tesisandroid;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tesisandroid.Fragment.BuscarFragment;
import com.example.tesisandroid.Fragment.HomeFragment;
import com.example.tesisandroid.Fragment.SubirFragment;

public class MenuPagerAdapter extends FragmentStateAdapter {

    public MenuPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new SubirFragment();
            case 2:
                return new BuscarFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Número de pestañas
    }
}