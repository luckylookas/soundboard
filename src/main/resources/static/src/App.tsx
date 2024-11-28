import React, {useState} from 'react';
import AdventureForm from "./AdventureForm";
import {Adventure} from "./api";
import {AdventureContext} from "./Context";


function App() {
    const adventure = useState<Adventure|undefined>()

    return <AdventureContext.Provider value={adventure}>
    <div id="body" className='flex justify-center items-start h-full p-5 bg-emerald-50'>
        <div className={`h-full w-full`}>
            <AdventureForm />
        </div>
    </div>
    </AdventureContext.Provider>
}

export default App;
