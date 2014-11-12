FloatingActionButton
====================
This project is forked from [futuresimple/FloatingActionButton](https://github.com/futuresimple/android-floating-action-button).

And I add some special characteristics which I "stoled" from this project[makovkastar/FloatingActionButton](https://github.com/makovkastar/FloatingActionButton)

Before I did this, I opened an [issue](https://github.com/futuresimple/android-floating-action-button/issues/27) to ask "is there any plan to add ScrollListener?". Unfortunately, i havn't get reply.This the reason I did the job~

New Feature
===
 When srolled down , the FloatingActionButton or FloatingActionsMenu would be hiden.


ScreenShot
===


Usage
===
	ListView listView = (ListView) root.findViewById(android.R.id.list);
    FloatingActionsMenu fam = (FloatingActionsMenu)root.findViewById(R.id.multiple_actions);
    fam.attachToListView(listView);
As for other usage you should go to the original project!

Other
===
```all rights unreserved,I just play a role as Porter```


License
=======

    Copyright (C) 2014 Jerzy Chalupski

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.