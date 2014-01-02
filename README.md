# ButterKnifeZelezny

Simple plug-in for Android Studio that allows one-click creation of [Butterknife](https://github.com/JakeWharton/butterknife) view injections.

## How to install

- in Android Studio: go to `Preferences → Plugins → Browse repositories` and search for `ButterKnife Zelezny`

_or_

- [download it](http://plugins.jetbrains.com/plugin/7369) and install via `Preferences → Plugins → Install plugin from disk`


## How to use

1) Right click on usage of desired layout reference (e.g. R.layout.main in your Activity or Fragment), then `Generate` and `Generate ButterKnife Injections`

 ![](img/generate.png)
 
2) Pick injections you want, you also have an option to create ViewHolder for adapters.

 ![](img/injections.png)
 
3) Click `Confirm` and enjoy injections in your code with no work!

## How to build the code

- follow [Getting Started with Plugin Development](http://confluence.jetbrains.com/display/IDEADEV/Getting+Started+with+Plugin+Development)
- make sure you have Java 6 installed if you want to publish it in the plugin repository

## Other's work

- IDEA code generator by Anatoly Korniltsev [https://github.com/kurganec/intellij-android-codegenerator/]

## Why 'Zelezny'?

<img src="http://assets.espn.go.com/i/oly/summer08/afp/xml/en/biop/images/bio/15525.jpg" width="60"  align="right"/>

[Jan Železný](http://en.wikipedia.org/wiki/Jan_%C5%BDelezn%C3%BD) is a famous Czech javelin thrower, Olympic champion and world record holder. With Zelezny's javelin, your butter knife will be much sharper!
