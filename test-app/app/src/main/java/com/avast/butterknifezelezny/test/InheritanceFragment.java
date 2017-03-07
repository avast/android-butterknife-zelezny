package com.avast.butterknifezelezny.test;

public class InheritanceFragment extends BaseFragment {

    public InheritanceFragment() {
    }

    @Override
    protected int getLayoutId() {
        // Try to generate Fragment injections by clicking to R.layout.fragment_main
        return R.layout.fragment_main;
    }
}