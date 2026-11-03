package com.learner.apps.crashingactivityandlifecycle

class StandardActivity : BaseActivity() { override val modeName = "standard" }
class SingleTopActivity : BaseActivity() { override val modeName = "singleTop" }
class SingleTaskActivity : BaseActivity() { override val modeName = "singleTask" }
class SingleInstanceActivity : BaseActivity() { override val modeName = "singleInstance" }