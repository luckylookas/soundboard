import React, {useContext, useState} from 'react';
import {Box, HtmlSearchInput} from "./components/Button";
import {AdventureContext} from "./Context";

function AdventureForm() {
    const [search, setSearch] = useState<string | undefined>('')

    const [adventure, setAdventure] = useContext(AdventureContext)

    return <Box onBack={() => setAdventure(undefined)} title={adventure ? adventure.name : "Adventures"}>
        {adventure ?
        <div>
            {adventure.id}...
        </div>
        :
            <HtmlSearchInput
                onSelect={(s) => setAdventure(s)}
                onChange={(s) => {
                    setSearch(s)
                }} id={'search'} value={search} inputProps={{}} additionalValues={[
                {name: "pub crawl epic", id: 1, scenes: []},
                {name: "a cave ", id: 2, scenes: []}
            ]}/>
        }
    </Box>
}

export default AdventureForm;
