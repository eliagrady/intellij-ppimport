# Polopoly Importer Plugin for IntelliJ

Based on Marc Viane's work (https://github.com/mavicon/intellij-ppimport) and inspiration
from Johan Rylander's Atex Polopoly plugin for IntelliJ (https://github.com/jrylander/PolopolyIntelliJPlugin).

This plugin is written to simplify import of content XML files into the Polopoly CMS.

## Key features

* import a single file or import multiple file selections
* import not only from the editor, but also from project tool window
* multiple files can be imported in bulk (as a jar) (specific Polopoly functionality)
* specify multiple targets (e.g. local, dev, staging and production) directly accessible from the context menu
* specify file extensions to import
* protect against accidental imports by asking for a confirmation (per target configurable)

This plugin stores the configuration at the IntelliJ application level, so they are available in all IntelliJ projects.
This is different from Johan or Marc's plugin, which store this information separately for each project.

## Change log

    0.7 Added ability to Cancel the background task.
        Added import actions to the VCS Changes popup menu.
		0.6 Restore _Import finished_ message.
		    Do progress update for a single file.
    0.5 Show progress dialog.
        Changed _Import successful_ to _Import finished_.
    0.4 Recurse files in alphabetic order.
        Less notifications about successful imports.
    0.3 Do imports asynchronously to avoid hanging IDE.
    0.2 Fixed configuration storage.
    0.1 Initial release.

## Installing

(Requires IntelliJ IDEA 11 or later.)

Download the **polopoly-import-0.7.jar** from the **distributable** folder.

1. Start IntelliJ IDEA.
2. Open the plugin manager dialog (**Menu: File > Settings > Plugins**)
3. Click **Install plugin from disk** button to open the **Choose Plugin file** dialog.
4. Select the **polopoly-import-0.7.jar** file and click **OK** to close the dialog.
5. Click **OK** to close the **Settings** dialog.
6. Restart IntelliJ IDEA.

## Legal

The source is released under the Apache License, Version 2.0.
