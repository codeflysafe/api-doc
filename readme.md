# api-doc


## Aim

This repository aim to auto-generate the web api 
document。 

- easy to use
- non-invasive for Java source

Meantime , this support to generate the Json source 
for export to `yapi`. It works via the scripts provided also.


## Why

There are so many brilliant project for java-api-doc generator,why another project to be done.

The man reason is that i want a easy and non-invasive tool.


## how to use ? 



### Java Doc Standard

### method

- `@param` : the parameter of method
- `@return` : the return info



### Extend Java Doc 

#### `@necessary`:
> The parameter is necessary or not for current method ;
  
   - `ethod`
   
#### `@ignore`: 
> The filed or the method will be ignore or not ;
  
   - `Field`,`Class`,`Method` ,`Parameter` 
   
In filed:


```java

   // line tag
   /**
   * @ignore
   * /
   private int number;
   
   // inline tag
   /**
   * @param {@ignore} {@fuzzy}
   */

```



  
    
#### `@name` 
> the name of current entity(contains class,method )
    - `Method`,`class`


## TODO LIST

- Extend  the Java doc



## Reference 

dependencies:

- [javaParser](https://github.com/javaparser/javaparser)

- [yapi](https://github.com/YMFE/yapi)

- git hook

- [java doc](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html)