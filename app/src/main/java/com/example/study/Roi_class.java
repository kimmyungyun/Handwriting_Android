package com.example.study;

import org.opencv.core.Rect;

//우선순위 큐를 이용해 사람이 글자를 읽듯이 순서를 매기려고 했으나
//순서를 매기는 알고리즘을 구현하지 못해서 사용을 못함
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
