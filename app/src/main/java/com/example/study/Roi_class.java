package com.example.study;

import org.opencv.core.Rect;

class Roi_class implements Comparable<Roi_class>{
    Rect rect;
    public Roi_class(Rect rect){
        this.rect = rect;
    }

    @Override
    public int compareTo(Roi_class target){
        if(this.rect.x > target.rect.x)
        {
            if(this.rect.y > target.rect.y)
            {
                return 1;
            }
            else if(this.rect.y < target.rect.y)
            {
                return -1;
            }
        }
       else if(this.rect.x < target.rect.x)
        {
            return -1;
        }
        return 0;
    }
}
