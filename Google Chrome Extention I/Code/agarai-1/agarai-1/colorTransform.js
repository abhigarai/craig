/*
This is used to link the attribute bg-color to the current page and changing the default view i.e. bgcolor attribute by checking the 

properties defaultview and getcomputedstyle.

*/

function getAttrStyle(elem,attr){
    if(elem.style[attr]){
        
        return elem.style[attr];
    }else if(elem.currentStyle){
        
        return elem.currentStyle[attr];
    }else if(document.defaultView && document.defaultView.getComputedStyle){
        
        attr=attr.replace(/([A-Z])/g,'-$1').toLowerCase();
        
        return document.defaultView.getComputedStyle(elem,null).getPropertyValue(attr);
    }else{
        return null;
    }
}
