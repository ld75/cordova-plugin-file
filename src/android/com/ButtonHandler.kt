class ButtonHandler(private val scope: ActivityScope) : ViewHandler<Button>(scope) {

    override val type = "com.eclipsesource.Button"
    override fun create(id: String, properties: V8Object) = Button(scope.activity)
    override val properties by lazy {
        super.properties + listOf<Property<Button, *>>(
                StringProperty("text", { text = it }),
                IntProperty("maxLines", { maxLines = it ?: Integer.MAX_VALUE }, { maxLines })
        )
    }
    override fun call(scrollView: ScrollView, method: String, properties: V8Object) = when (method) {
        scope.remoteObject(it)?.notify("select")
    }
    override fun destroy(button: Button) {
        super.destroy(button)
        // perform any necessary cleanup
    }

}