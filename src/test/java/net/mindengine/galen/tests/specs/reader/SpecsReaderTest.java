/*******************************************************************************
* Copyright 2015 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package net.mindengine.galen.tests.specs.reader;


import static net.mindengine.galen.components.TestUtils.deleteSystemProperty;
import static net.mindengine.galen.specs.Side.BOTTOM;
import static net.mindengine.galen.specs.Side.LEFT;
import static net.mindengine.galen.specs.Side.RIGHT;
import static net.mindengine.galen.specs.Side.TOP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import net.mindengine.galen.browser.Browser;
import net.mindengine.galen.config.GalenConfig;
import net.mindengine.galen.page.Rect;
import net.mindengine.galen.parser.SyntaxException;
import net.mindengine.galen.specs.*;
import net.mindengine.galen.specs.colors.ColorRange;
import net.mindengine.galen.specs.reader.SpecReader;

import net.mindengine.rainbow4j.filters.BlurFilter;
import net.mindengine.rainbow4j.filters.DenoiseFilter;
import net.mindengine.rainbow4j.filters.SaturationFilter;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class SpecsReaderTest {

    
    private static final Browser NO_BROWSER = null;
    private static final Properties EMPTY_PROPERTIES = new Properties();

    @BeforeClass
    public void init() throws IOException {
        deleteSystemProperty("galen.range.approximation");
        deleteSystemProperty("galen.reporting.listeners");
        GalenConfig.getConfig().reset();
    }

    @BeforeMethod
    public void configureApproximation() {
        System.setProperty("galen.range.approximation", "2");
    }
    
    @AfterMethod
    public void clearApproximation() {
        System.getProperties().remove("galen.range.approximation");
    }

    @Test
    public void shouldReadSpec_inside() throws IOException {
        Spec spec = readSpec("inside: object");
        SpecInside specInside = (SpecInside) spec;

        assertThat(specInside.getObject(), is("object"));
        assertThat(specInside.getPartly(), is(false));

        List<Location> locations = specInside.getLocations();
        assertThat(locations.size(), is(0));
    }

    @Test
    public void shouldReadSpec_inside_object_10px_right() throws IOException {
        Spec spec = readSpec("inside: object 10px right");
        SpecInside specInside = (SpecInside) spec;

        assertThat(specInside.getObject(), is("object"));
        assertThat(specInside.getPartly(), is(false));
        
        List<Location> locations = specInside.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(specInside.getLocations(), contains(new Location(Range.exact(10), sides(RIGHT))));
        assertThat(spec.getOriginalText(), is("inside: object 10px right"));
    }
    
    @Test
    public void shouldReadSpec_inside_partly_object_10px_right()  throws IOException {
        Spec spec = readSpec("inside partly: object 10px right");
        SpecInside specInside = (SpecInside) spec;

        assertThat(specInside.getObject(), is("object"));
        assertThat(specInside.getPartly(), is(true));
        
        List<Location> locations = specInside.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(specInside.getLocations(), contains(new Location(Range.exact(10), sides(RIGHT))));
        assertThat(spec.getOriginalText(), is("inside partly: object 10px right"));
    }
    

    @Test
    public void shouldReadSpec_inside_object_10_to_30px_left()  throws IOException {
        Spec spec = readSpec("inside: object 10 to 30px left");
        SpecInside specInside = (SpecInside) spec;

        assertThat(specInside.getObject(), is("object"));
        
        List<Location> locations = specInside.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(specInside.getLocations(), contains(new Location(Range.between(10, 30), sides(LEFT))));
        assertThat(spec.getOriginalText(), is("inside: object 10 to 30px left"));
    }
    
    @Test
    public void shouldReadSpec_inside_object_25px_top_left()  throws IOException {
        SpecInside spec = (SpecInside)readSpec("inside: object 25px top left");
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(25),sides(TOP, LEFT))));
        assertThat(spec.getOriginalText(), is("inside: object 25px top left"));
    }
    
    @Test
    public void shouldReadSpec_inside_object_25px_top_left_comma_10_to_20px_bottom()  throws IOException {
        SpecInside spec = (SpecInside)readSpec("inside: object 25px top left, 10 to 20px bottom");
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(2));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(25),sides(TOP, LEFT)),
                new Location(Range.between(10, 20), sides(BOTTOM))));
        assertThat(spec.getOriginalText(), is("inside: object 25px top left, 10 to 20px bottom"));
    }
    
    @Test
    public void shouldReadSpec_inside_object_25px_bottom_right()  throws IOException {
        SpecInside spec = (SpecInside)readSpec("inside: object 25px bottom right");
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(25),sides(BOTTOM, RIGHT))));
        assertThat(spec.getOriginalText(), is("inside: object 25px bottom right"));
    }
    
    @Test
    public void shouldReadSpec_inside_object_25px_top_left_right_bottom()  throws IOException {
        SpecInside spec = (SpecInside)readSpec("inside: object 25px top left right bottom ");
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(25), sides(TOP, LEFT, RIGHT, BOTTOM))));
        assertThat(spec.getOriginalText(), is("inside: object 25px top left right bottom"));
    }
    
    @Test public void shouldReadSpec_inside_object_20px_left_and_approximate_30px_top()  throws IOException {
        SpecInside spec = (SpecInside)readSpec("inside: object 20px left, ~30px top");
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(2));
        
        Assert.assertEquals(new Location(Range.exact(20), sides(LEFT)), spec.getLocations().get(0));
        Assert.assertEquals(new Location(Range.between(28, 32), sides(TOP)), spec.getLocations().get(1));
        
        assertThat(spec.getOriginalText(), is("inside: object 20px left, ~30px top"));
    }
        
    @Test
    public void shouldReadSpec_contains()  throws IOException {
        Spec spec = readSpec("contains: object, menu, button");
        SpecContains specContains = (SpecContains) spec;
        assertThat(specContains.getChildObjects(), contains("object", "menu", "button"));
        assertThat(spec.getOriginalText(), is("contains: object, menu, button"));
    }
    
    @Test
    public void shouldReadSpec_contains_with_regex()  throws IOException {
        Spec spec = readSpec("contains: menu-item-*");
        SpecContains specContains = (SpecContains) spec;
        assertThat(specContains.getChildObjects(), contains("menu-item-*"));
        assertThat(spec.getOriginalText(), is("contains: menu-item-*"));
    }
    
    @Test
    public void shouldReadSpec_contains_partly()  throws IOException {
        Spec spec = readSpec("contains partly: object, menu, button");
        SpecContains specContains = (SpecContains) spec;
        assertThat(specContains.isPartly(), is(true));
        assertThat(specContains.getChildObjects(), contains("object", "menu", "button"));
        assertThat(spec.getOriginalText(), is("contains partly: object, menu, button"));
    }
    
    @Test 
    public void shouldReadSpec_near_button_10_to_20px_left()  throws IOException {
        SpecNear spec = (SpecNear) readSpec("near: button 10 to 20px left");
        
        assertThat(spec.getObject(), is("button"));
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.between(10, 20), sides(LEFT))));
        assertThat(spec.getOriginalText(), is("near: button 10 to 20px left"));
    }
    
    @Test 
    public void shouldReadSpec_near_button_10_to_20px_top_right()  throws IOException {
        SpecNear spec = (SpecNear) readSpec("near: button 10 to 20px top right");
        assertThat(spec.getObject(), is("button"));
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.between(10, 20), sides(TOP, RIGHT))));
        assertThat(spec.getOriginalText(), is("near: button 10 to 20px top right"));
    }
    
    @Test
    public void shouldReadSpec_near_button_approx_0px_left()  throws IOException {
        SpecNear spec = (SpecNear) readSpec("near: button ~0px left");
        assertThat(spec.getObject(), is("button"));
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.between(-2, 2), sides(LEFT))));
        assertThat(spec.getOriginalText(), is("near: button ~0px left"));
    }
    
    
    @Test
    public void shouldReadSpec_aligned_horizontally_centered()  throws IOException {
        SpecHorizontally spec = (SpecHorizontally) readSpec("aligned horizontally centered: object");
        assertThat(spec.getAlignment(), is(Alignment.CENTERED));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(0));
        assertThat(spec.getOriginalText(), is("aligned horizontally centered: object"));
    }
    
    @Test
    public void shouldReadSpec_aligned_horizontally_top()  throws IOException {
        SpecHorizontally spec = (SpecHorizontally) readSpec("aligned horizontally top: object");
        assertThat(spec.getAlignment(), is(Alignment.TOP));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(0));
        assertThat(spec.getOriginalText(), is("aligned horizontally top: object"));
    }
    
    @Test
    public void shouldReadSpec_aligned_horizontally_bottom()  throws IOException {
        SpecHorizontally spec = (SpecHorizontally) readSpec("aligned horizontally bottom: object");
        assertThat(spec.getAlignment(), is(Alignment.BOTTOM));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(0));
        assertThat(spec.getOriginalText(), is("aligned horizontally bottom: object"));
    }
    
    @Test
    public void shouldReadSpec_aligned_horizontally()  throws IOException {
        SpecHorizontally spec = (SpecHorizontally) readSpec("aligned horizontally: object");
        assertThat(spec.getAlignment(), is(Alignment.ALL));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(0));
        assertThat(spec.getOriginalText(), is("aligned horizontally: object"));
    }
    
    @Test
    public void shouldReadSpec_aligned_horizontally_all()  throws IOException {
        SpecHorizontally spec = (SpecHorizontally) readSpec("aligned horizontally all: object");
        assertThat(spec.getAlignment(), is(Alignment.ALL));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(0));
        assertThat(spec.getOriginalText(), is("aligned horizontally all: object"));
    }
    
    @Test
    public void shouldReadSpec_aligned_vertically_centered()  throws IOException {
        SpecVertically spec = (SpecVertically) readSpec("aligned  vertically  centered: object");
        assertThat(spec.getAlignment(), is(Alignment.CENTERED));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(0));
        assertThat(spec.getOriginalText(), is("aligned  vertically  centered: object"));
    }
    
    @Test
    public void shouldReadSpec_aligned_vertically_left()  throws IOException {
        SpecVertically spec = (SpecVertically) readSpec("aligned vertically left: object");
        assertThat(spec.getAlignment(), is(Alignment.LEFT));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(0));
        assertThat(spec.getOriginalText(), is("aligned vertically left: object"));
    }
    
    @Test
    public void shouldReadSpec_aligned_vertically_right()  throws IOException {
        SpecVertically spec = (SpecVertically) readSpec("aligned vertically right: object");
        assertThat(spec.getAlignment(), is(Alignment.RIGHT));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(0));
        assertThat(spec.getOriginalText(), is("aligned vertically right: object"));
    }
    
    @Test
    public void shouldReadSpec_aligned_vertically()  throws IOException {
        SpecVertically spec = (SpecVertically) readSpec("aligned vertically: object");
        assertThat(spec.getAlignment(), is(Alignment.ALL));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(0));
        assertThat(spec.getOriginalText(), is("aligned vertically: object"));
    }
    
    @Test
    public void shouldReadSpec_aligned_vertically_all()  throws IOException {
        SpecVertically spec = (SpecVertically) readSpec("aligned vertically all: object");
        assertThat(spec.getAlignment(), is(Alignment.ALL));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(0));
        assertThat(spec.getOriginalText(), is("aligned vertically all: object"));
    }
    
    @Test
    public void shouldReadSpec_aligned_vertically_with_error_rate_10px()  throws IOException {
        SpecVertically spec = (SpecVertically) readSpec("aligned vertically all: object 10px");
        assertThat(spec.getAlignment(), is(Alignment.ALL));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(10));
        assertThat(spec.getOriginalText(), is("aligned vertically all: object 10px"));
    }
    
    @Test
    public void shouldReadSpec_aligned_vertically_with_error_rate_10_px()  throws IOException {
        SpecVertically spec = (SpecVertically) readSpec("aligned vertically all: object 10  px");
        assertThat(spec.getAlignment(), is(Alignment.ALL));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(10));
        assertThat(spec.getOriginalText(), is("aligned vertically all: object 10  px"));
    }
    
    @Test
    public void shouldReadSpec_aligned_horizontally_with_error_rate_10px()  throws IOException {
        SpecHorizontally spec = (SpecHorizontally) readSpec("aligned horizontally all: object 10px");
        assertThat(spec.getAlignment(), is(Alignment.ALL));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(10));
        assertThat(spec.getOriginalText(), is("aligned horizontally all: object 10px"));
    }
    
    @Test
    public void shouldReadSpec_aligned_horizontally_with_error_rate_10_px()  throws IOException {
        SpecHorizontally spec = (SpecHorizontally) readSpec("aligned horizontally all: object 10 px");
        assertThat(spec.getAlignment(), is(Alignment.ALL));
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getErrorRate(), is(10));
        assertThat(spec.getOriginalText(), is("aligned horizontally all: object 10 px"));
    }
    
    @Test
    public void shouldReadSpec_absent()  throws IOException {
        Spec spec = readSpec("absent");
        assertThat(spec, Matchers.instanceOf(SpecAbsent.class));
        assertThat(spec.getOriginalText(), is("absent"));
    }
    
    @Test
    public void shouldReadSpec_visible()  throws IOException {
        Spec spec = readSpec("visible");
        assertThat(spec, Matchers.instanceOf(SpecVisible.class));
        assertThat(spec.getOriginalText(), is("visible"));
    }
    
    @Test
    public void shouldReadSpec_width_10px()  throws IOException {
        SpecWidth spec = (SpecWidth) readSpec("width: 10px");
        assertThat(spec.getRange(), is(Range.exact(10)));
        assertThat(spec.getOriginalText(), is("width: 10px"));
    }
    
    @Test
    public void shouldReadSpec_width_5_to_8px()  throws IOException {
        SpecWidth spec = (SpecWidth) readSpec("width: 5 to 8px");
        assertThat(spec.getRange(), is(Range.between(5, 8)));
        assertThat(spec.getOriginalText(), is("width: 5 to 8px"));
    }
    
    @Test
    public void shouldReadSpec_width_100_percent_of_other_object_width()  throws IOException {
        SpecWidth spec = (SpecWidth) readSpec("width: 100% of main-big-container/width");
        assertThat(spec.getRange(), is(Range.exact(100).withPercentOf("main-big-container/width")));
        assertThat(spec.getOriginalText(), is("width: 100% of main-big-container/width"));
    }
    
    @Test
    public void shouldReadSpec_height_10px()  throws IOException {
        SpecHeight spec = (SpecHeight) readSpec("height: 10px");
        assertThat(spec.getRange(), is(Range.exact(10)));
        assertThat(spec.getOriginalText(), is("height: 10px"));
    }
    
    @Test
    public void shouldReadSpec_height_5_to_8px()  throws IOException {
        SpecHeight spec = (SpecHeight) readSpec("height: 5 to 8px");
        assertThat(spec.getRange(), is(Range.between(5, 8)));
        assertThat(spec.getOriginalText(), is("height: 5 to 8px"));
    }
    
    @Test
    public void shouldReadSpec_text_is_some_text()  throws IOException {
        SpecText spec = (SpecText)readSpec("text is:  Some text ");
        assertThat(spec.getText(), is("Some text"));
        assertThat(spec.getType(), is(SpecText.Type.IS));
    }
    
    @Test
    public void shouldReadSpec_text_is_some_text_2()  throws IOException {
        SpecText spec = (SpecText)readSpec("text is:Some text with colon:");
        assertThat(spec.getText(), is("Some text with colon:"));
        assertThat(spec.getType(), is(SpecText.Type.IS));
    }
    
    @Test
    public void shouldReadSpec_text_is_empty()  throws IOException {
        SpecText spec = (SpecText)readSpec("text is:  ");
        assertThat(spec.getText(), is(""));
        assertThat(spec.getType(), is(SpecText.Type.IS));
    }
    
    @Test
    public void shouldReadSpec_text_is_empty_2()  throws IOException {
        SpecText spec = (SpecText)readSpec("text is:");
        assertThat(spec.getText(), is(""));
        assertThat(spec.getType(), is(SpecText.Type.IS));
    }
    
    @Test
    public void shouldReadSpec_text_contains_some_text()  throws IOException {
        SpecText spec = (SpecText)readSpec("text contains:  Some text ");
        assertThat(spec.getText(), is("Some text"));
        assertThat(spec.getType(), is(SpecText.Type.CONTAINS));
    }
    
    @Test
    public void shouldReadSpec_text_startsWith_some_text()  throws IOException {
        SpecText spec = (SpecText)readSpec("text starts:  Some text ");
        assertThat(spec.getText(), is("Some text"));
        assertThat(spec.getType(), is(SpecText.Type.STARTS));
    }
    
    @Test
    public void shouldReadSpec_text_endssWith_some_text()  throws IOException {
        SpecText spec = (SpecText)readSpec("text ends:  Some text ");
        assertThat(spec.getText(), is("Some text"));
        assertThat(spec.getType(), is(SpecText.Type.ENDS));
    }
    
    @Test
    public void shouldReadSpec_text_matches_some_text()  throws IOException {
        SpecText spec = (SpecText)readSpec("text matches:  Some * text ");
        assertThat(spec.getText(), is("Some * text"));
        assertThat(spec.getType(), is(SpecText.Type.MATCHES));
    }

    @Test
    public void shouldReadSpec_text_lowercase_is() throws IOException {
        SpecText spec = (SpecText)readSpec("text lowercase is: some text");
        assertThat(spec.getText(), is("some text"));
        assertThat(spec.getType(), is(SpecText.Type.IS));
        assertThat(spec.getOperations(), contains("lowercase"));
    }

    @Test
    public void shouldReadSpec_text_lowercase_uppercase_is() throws IOException {
        SpecText spec = (SpecText)readSpec("text lowercase uppercase is: SOME TEXT");
        assertThat(spec.getText(), is("SOME TEXT"));
        assertThat(spec.getType(), is(SpecText.Type.IS));
        assertThat(spec.getOperations(), contains("lowercase", "uppercase"));
    }

    @Test
    public void shouldReadSpec_css_fontsize_is_18px() throws IOException {
        SpecCss spec = (SpecCss)readSpec("css font-size is: 18px");
        assertThat(spec.getCssPropertyName(), is("font-size"));
        assertThat(spec.getText(), is("18px"));
        assertThat(spec.getType(), is(SpecText.Type.IS));
    }

    @Test
    public void shouldReadSpec_css_fontsize_starts() throws IOException {
        SpecCss spec = (SpecCss)readSpec("css font-size starts: 18px");
        assertThat(spec.getCssPropertyName(), is("font-size"));
        assertThat(spec.getText(), is("18px"));
        assertThat(spec.getType(), is(SpecText.Type.STARTS));
    }

    @Test
    public void shouldReadSpec_css_fontsize_ends() throws IOException {
        SpecCss spec = (SpecCss)readSpec("css font-size ends: 18px");
        assertThat(spec.getCssPropertyName(), is("font-size"));
        assertThat(spec.getText(), is("18px"));
        assertThat(spec.getType(), is(SpecText.Type.ENDS));
    }

    @Test
    public void shouldReadSpec_css_fontsize_contains() throws IOException {
        SpecCss spec = (SpecCss)readSpec("css font-size contains: 18px");
        assertThat(spec.getCssPropertyName(), is("font-size"));
        assertThat(spec.getText(), is("18px"));
        assertThat(spec.getType(), is(SpecText.Type.CONTAINS));
    }

    @Test
    public void shouldReadSpec_css_fontsize_matches() throws IOException {
        SpecCss spec = (SpecCss)readSpec("css font-size matches: 18px");
        assertThat(spec.getCssPropertyName(), is("font-size"));
        assertThat(spec.getText(), is("18px"));
        assertThat(spec.getType(), is(SpecText.Type.MATCHES));
    }


    @Test(expectedExceptions = {SyntaxException.class},
            expectedExceptionsMessageRegExp = "Missing css property name"
    )
    public void shouldGiveException_empty_css_spec() throws IOException {
        readSpec("css : 18px");
    }

    @Test(expectedExceptions = {SyntaxException.class},
            expectedExceptionsMessageRegExp = "Missing validation type \\(is, contains, starts, ends, matches\\)"
    )
    public void shouldGiveException_css_without_type() throws IOException {
        readSpec("css font-size: 18px");
    }


    
    @Test 
    public void shouldReadSpec_above_object_20px()  throws IOException {
    	SpecAbove spec = (SpecAbove)readSpec("above: object 20px");
    	assertThat(spec.getObject(), is("object"));
    	assertThat(spec.getRange(), is(Range.exact(20)));
    }
    
    @Test 
    public void shouldReadSpec_above_object_10_20px()  throws IOException {
    	SpecAbove spec = (SpecAbove)readSpec("above: object 10 to 20px");
    	assertThat(spec.getObject(), is("object"));
    	assertThat(spec.getRange(), is(Range.between(10, 20)));
    }
    
    @Test 
    public void shouldReadSpec_above()  throws IOException {
        SpecAbove spec = (SpecAbove)readSpec("above: object");
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getRange(), is(Range.greaterThan(-1.0)));
    }
    
    @Test 
    public void shouldReadSpec_below()  throws IOException {
        SpecBelow spec = (SpecBelow)readSpec("below: object");
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getRange(), is(Range.greaterThan(-1.0)));
    }
    
    @Test 
    public void shouldReadSpec_below_object_20px()  throws IOException {
    	SpecBelow spec = (SpecBelow)readSpec("below: object 20px");
    	assertThat(spec.getObject(), is("object"));
    	assertThat(spec.getRange(), is(Range.exact(20)));
    }
    
    @Test 
    public void shouldReadSpec_below_object_10_to_20px()  throws IOException {
    	SpecBelow spec = (SpecBelow)readSpec("below: object 10 to 20px");
    	assertThat(spec.getObject(), is("object"));
    	assertThat(spec.getRange(), is(Range.between(10, 20)));
    }


    @Test
    public void shouldReadSpec_left_of_object_10px() throws IOException {
        SpecLeftOf specLeftOf = (SpecLeftOf)readSpec("left of: object 10px");
        assertThat(specLeftOf.getObject(), is("object"));
        assertThat(specLeftOf.getRange(), is(Range.exact(10)));
    }

    @Test
    public void shouldReadSpec_left_of_object_10_to_20px() throws IOException {
        SpecLeftOf specLeftOf = (SpecLeftOf)readSpec("left of: object 10 to 20px");
        assertThat(specLeftOf.getObject(), is("object"));
        assertThat(specLeftOf.getRange(), is(Range.between(10, 20)));
    }

    @Test
    public void shouldReadSpec_left___of_object_10px() throws IOException {
        SpecLeftOf specLeftOf = (SpecLeftOf)readSpec("left   \tof  : object 10px");
        assertThat(specLeftOf.getObject(), is("object"));
        assertThat(specLeftOf.getRange(), is(Range.exact(10)));
    }

    @Test
    public void shouldReadSpec_left_of_object() throws IOException {
        SpecLeftOf specLeftOf = (SpecLeftOf)readSpec("left of: object");
        assertThat(specLeftOf.getObject(), is("object"));
        assertThat(specLeftOf.getRange(), is(Range.greaterThan(-1.0)));
    }


    @Test
    public void shouldReadSpec_right_of_object_10px() throws IOException {
        SpecRightOf specRightOf = (SpecRightOf)readSpec("right of: object 10px");
        assertThat(specRightOf.getObject(), is("object"));
        assertThat(specRightOf.getRange(), is(Range.exact(10)));
    }

    @Test
    public void shouldReadSpec_right_of_object_10_to_20px() throws IOException {
        SpecRightOf specRightOf = (SpecRightOf)readSpec("right of: object 10 to 20px");
        assertThat(specRightOf.getObject(), is("object"));
        assertThat(specRightOf.getRange(), is(Range.between(10, 20)));
    }

    @Test
    public void shouldReadSpec_right___of_object_10px() throws IOException {
        SpecRightOf specRightOf = (SpecRightOf)readSpec("right   \tof  : object 10px");
        assertThat(specRightOf.getObject(), is("object"));
        assertThat(specRightOf.getRange(), is(Range.exact(10)));
    }

    @Test
    public void shouldReadSpec_right_of_object() throws IOException {
        SpecRightOf specRightOf = (SpecRightOf)readSpec("right of: object");
        assertThat(specRightOf.getObject(), is("object"));
        assertThat(specRightOf.getRange(), is(Range.greaterThan(-1.0)));
    }

    @Test 
    public void shouldReadSpec_centered_inside_object()  throws IOException {
    	SpecCentered spec = (SpecCentered)readSpec("centered inside: object");
    	assertThat(spec.getObject(), is("object"));
    	assertThat(spec.getLocation(), is(SpecCentered.Location.INSIDE));
    	assertThat(spec.getAlignment(), is(SpecCentered.Alignment.ALL));
    }
    
    @Test 
    public void shouldReadSpec_centered_horizontally_inside_object()  throws IOException {
    	SpecCentered spec = (SpecCentered)readSpec("centered horizontally inside: object");
    	assertThat(spec.getObject(), is("object"));
    	assertThat(spec.getLocation(), is(SpecCentered.Location.INSIDE));
    	assertThat(spec.getAlignment(), is(SpecCentered.Alignment.HORIZONTALLY));
    }
    
    @Test 
    public void shouldReadSpec_centered_vertically_inside_object()  throws IOException {
    	SpecCentered spec = (SpecCentered)readSpec("centered vertically inside: object");
    	assertThat(spec.getObject(), is("object"));
    	assertThat(spec.getLocation(), is(SpecCentered.Location.INSIDE));
    	assertThat(spec.getAlignment(), is(SpecCentered.Alignment.VERTICALLY));
    }
    
    @Test 
    public void shouldReadSpec_centered_all_inside_object()  throws IOException {
        SpecCentered spec = (SpecCentered)readSpec("centered all inside: object");
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getLocation(), is(SpecCentered.Location.INSIDE));
        assertThat(spec.getAlignment(), is(SpecCentered.Alignment.ALL));
    }
    
    @Test 
    public void shouldReadSpec_centered_all_on_object()  throws IOException {
        SpecCentered spec = (SpecCentered)readSpec("centered all on: object");
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getLocation(), is(SpecCentered.Location.ON));
        assertThat(spec.getAlignment(), is(SpecCentered.Alignment.ALL));
    }
    
    @Test 
    public void shouldReadSpec_centered_on_object()  throws IOException {
    	SpecCentered spec = (SpecCentered)readSpec("centered on: object");
    	assertThat(spec.getObject(), is("object"));
    	assertThat(spec.getLocation(), is(SpecCentered.Location.ON));
    	assertThat(spec.getAlignment(), is(SpecCentered.Alignment.ALL));
    }
    
    @Test 
    public void shouldReadSpec_centered_horizontally_on_object()  throws IOException {
    	SpecCentered spec = (SpecCentered)readSpec("centered horizontally on: object");
    	assertThat(spec.getObject(), is("object"));
    	assertThat(spec.getLocation(), is(SpecCentered.Location.ON));
    	assertThat(spec.getAlignment(), is(SpecCentered.Alignment.HORIZONTALLY));
    }
    
    @Test 
    public void shouldReadSpec_centered_horizontally_on_object_25px() throws IOException {
        SpecCentered spec = (SpecCentered)readSpec("centered horizontally on: object 25px");
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getLocation(), is(SpecCentered.Location.ON));
        assertThat(spec.getAlignment(), is(SpecCentered.Alignment.HORIZONTALLY));
        assertThat(spec.getErrorRate(), is(25));
    }
    
    @Test 
    public void shouldReadSpec_centered_horizontally_on_object_25_px() throws IOException {
        SpecCentered spec = (SpecCentered)readSpec("centered horizontally on: object 25 px");
        assertThat(spec.getObject(), is("object"));
        assertThat(spec.getLocation(), is(SpecCentered.Location.ON));
        assertThat(spec.getAlignment(), is(SpecCentered.Alignment.HORIZONTALLY));
        assertThat(spec.getErrorRate(), is(25));
    }
    
    @Test 
    public void shouldReadSpec_centered_vertically_on_object() throws IOException {
    	SpecCentered spec = (SpecCentered)readSpec("centered vertically on: object");
    	assertThat(spec.getObject(), is("object"));
    	assertThat(spec.getLocation(), is(SpecCentered.Location.ON));
    	assertThat(spec.getAlignment(), is(SpecCentered.Alignment.VERTICALLY));
    }
    
    @Test
    public void shoulReadSpec_on_object_10px_left() throws IOException {
        SpecOn spec = (SpecOn)readSpec("on: object 10px left");
        
        assertThat(spec.getSideHorizontal(), is(TOP));
        assertThat(spec.getSideVertical(), is(LEFT));
        assertThat(spec.getObject(), is("object"));
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(10), sides(LEFT))));
        assertThat(spec.getOriginalText(), is("on: object 10px left"));
    }
    
    @Test
    public void shoulReadSpec_on_object_10px_left_20px_top() throws IOException {
        SpecOn spec = (SpecOn)readSpec("on: object 10px left, 20px top");
        
        assertThat(spec.getSideHorizontal(), is(TOP));
        assertThat(spec.getSideVertical(), is(LEFT));
        assertThat(spec.getObject(), is("object"));
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(2));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(10), sides(LEFT)), new Location(Range.exact(20), sides(TOP))));
        assertThat(spec.getOriginalText(), is("on: object 10px left, 20px top"));
    }
    
    @Test
    public void shouldReadSpec_on_top_object_10px_top_right() throws Exception {
        SpecOn spec = (SpecOn)readSpec("on top: object 10px top right");
        
        assertThat(spec.getSideHorizontal(), is(TOP));
        assertThat(spec.getSideVertical(), is(LEFT));
        assertThat(spec.getObject(), is("object"));
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(10), sides(TOP, RIGHT))));
        assertThat(spec.getOriginalText(), is("on top: object 10px top right"));
    }
    
    @Test
    public void shouldReadSpec_on_left_object_10px_top_right() throws Exception {
        SpecOn spec = (SpecOn)readSpec("on left: object 10px top right");
        
        assertThat(spec.getSideHorizontal(), is(TOP));
        assertThat(spec.getSideVertical(), is(LEFT));
        assertThat(spec.getObject(), is("object"));
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(10), sides(TOP, RIGHT))));
        assertThat(spec.getOriginalText(), is("on left: object 10px top right"));
    }
    
    @Test
    public void shouldReadSpec_on_bottom_right_object_10px_top_right() throws Exception {
        SpecOn spec = (SpecOn)readSpec("on right bottom: object 10px top right");
        
        assertThat(spec.getSideHorizontal(), is(BOTTOM));
        assertThat(spec.getSideVertical(), is(RIGHT));
        assertThat(spec.getObject(), is("object"));
        
        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(10), sides(TOP, RIGHT))));
        assertThat(spec.getOriginalText(), is("on right bottom: object 10px top right"));
    }
    
    @Test
    public void shouldReadSpec_color_scheme_40percent_black_approx_30percent_white() throws Exception {
        SpecColorScheme spec = (SpecColorScheme)readSpec("color scheme: 40% black  , ~30% white");
        List<ColorRange> colors = spec.getColorRanges();
        assertThat(colors.size(), is(2));
        
        assertThat(colors.get(0).getRange(), is(Range.exact(40)));
        assertThat(colors.get(0).getColor(), is(new Color(0, 0, 0)));
        assertThat(colors.get(1).getRange(), is(Range.between(28, 32)));
        assertThat(colors.get(1).getColor(), is(new Color(255, 255, 255)));
    }
    
    @Test
    public void shouldReadSpec_color_scheme_greater_than_40percent_ffaa03() throws Exception {
        SpecColorScheme spec = (SpecColorScheme)readSpec("color scheme: > 40% #ffaa03");
        List<ColorRange> colors = spec.getColorRanges();
        assertThat(colors.size(), is(1));
        
        assertThat(colors.get(0).getRange(), is(Range.greaterThan(40.0)));
        assertThat(colors.get(0).getColor(), is(new Color(255, 170, 3)));
    }
    
    @Test
    public void shouldReadSpec_color_scheme_40_to_50percent_ffaa03() throws Exception {
        SpecColorScheme spec = (SpecColorScheme)readSpec("color scheme: 40 to 50% red");
        List<ColorRange> colors = spec.getColorRanges();
        assertThat(colors.size(), is(1));
        
        assertThat(colors.get(0).getRange(), is(Range.between(40, 50)));
        assertThat(colors.get(0).getColor(), is(new Color(255, 0, 0)));
    }

    @Test
    public void shouldReadSpec_image_withMaxPercentageError() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, error 2.4%");
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.getErrorRate().getValue(), is(2.4));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PERCENT));
        assertThat(spec.getTolerance(), is(25));
    }

    @Test
    public void shouldReadSpec_image_withMaxPixelsError() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, error 112 px");
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.getErrorRate().getValue(), is(112.0));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PIXELS));
        assertThat(spec.getTolerance(), is(25));
    }

    @Test
    public void shouldReadSpec_image_withMaxPixelsError_tolerance5() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, error 112 px, tolerance 5");
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.getErrorRate().getValue(), is(112.0));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PIXELS));
        assertThat(spec.getTolerance(), is(5));
        assertThat(spec.isStretch(), is(false));
        assertThat(spec.isCropIfOutside(), is(false));
    }

    @Test
    public void shouldReadSpec_image_withMaxPixelsError_tolerance5_stretch() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, error 112 px, tolerance 5, stretch");
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.getErrorRate().getValue(), is(112.0));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PIXELS));
        assertThat(spec.getTolerance(), is(5));
        assertThat(spec.isStretch(), is(true));
    }

    @Test
    public void shouldReadSpec_image_withCropIfOutside() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, crop-if-outside");
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.isCropIfOutside(), is(true));
    }

    @Test
    public void shouldReadSpec_image_withMaxPixelsError_tolerance5_filterBlur2() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, error 112 px, tolerance 5, filter blur 2");
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.getErrorRate().getValue(), is(112.0));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PIXELS));
        assertThat(spec.getTolerance(), is(5));
        assertThat(spec.getOriginalFilters().size(), is(1));
        assertThat(spec.getSampleFilters().size(), is(1));

        assertThat(((BlurFilter)spec.getOriginalFilters().get(0)).getRadius(), is(2));
        assertThat(((BlurFilter)spec.getSampleFilters().get(0)).getRadius(), is(2));
    }

    @Test
    public void shouldReadSpec_image_withMaxPixelsError_tolerance5_filterABlur2() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, error 112 px, tolerance 5, filter-a blur 2");
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.getErrorRate().getValue(), is(112.0));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PIXELS));
        assertThat(spec.getTolerance(), is(5));
        assertThat(spec.getOriginalFilters().size(), is(1));
        assertThat(spec.getSampleFilters().size(), is(0));

        assertThat(((BlurFilter)spec.getOriginalFilters().get(0)).getRadius(), is(2));
    }

    @Test
    public void shouldReadSpec_image_withMaxPixelsError_tolerance5_filterBBlur2() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, error 112 px, tolerance 5, filter-b blur 2");
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.getErrorRate().getValue(), is(112.0));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PIXELS));
        assertThat(spec.getTolerance(), is(5));
        assertThat(spec.getOriginalFilters().size(), is(0));
        assertThat(spec.getSampleFilters().size(), is(1));

        assertThat(((BlurFilter)spec.getSampleFilters().get(0)).getRadius(), is(2));
    }


    @Test
    public void shouldReadSpec_image_withMaxPixelsError_tolerance5_filterBlur2_filterDenoise1() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, error 112 px, filter blur 2, filter denoise 4, tolerance 5");
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.getErrorRate().getValue(), is(112.0));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PIXELS));
        assertThat(spec.getTolerance(), is(5));

        assertThat(spec.getOriginalFilters().size(), is(2));

        BlurFilter filter1 = (BlurFilter) spec.getOriginalFilters().get(0);
        assertThat(filter1.getRadius(), is(2));

        DenoiseFilter filter2 = (DenoiseFilter) spec.getOriginalFilters().get(1);
        assertThat(filter2.getRadius(), is(4));
    }

    @Test
    public void shouldReadSpec_image_withMaxPixelsError_tolerance5_filterBlur2_filterSaturation10_mapFilterDenoise1() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, error 112 px, filter blur 2, filter saturation 10, map-filter denoise 4, tolerance 5");
        assertThat(spec.getErrorRate().getValue(), is(112.0));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PIXELS));
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.getTolerance(), is(5));

        assertThat(spec.getOriginalFilters().size(), is(2));
        assertThat(spec.getSampleFilters().size(), is(2));
        assertThat(spec.getMapFilters().size(), is(1));

        assertThat(((BlurFilter)spec.getOriginalFilters().get(0)).getRadius(), is(2));
        assertThat(((BlurFilter)spec.getSampleFilters().get(0)).getRadius(), is(2));

        assertThat(((SaturationFilter)spec.getOriginalFilters().get(1)).getLevel(), is(10));
        assertThat(((SaturationFilter)spec.getSampleFilters().get(1)).getLevel(), is(10));

        DenoiseFilter filter2 = (DenoiseFilter) spec.getMapFilters().get(0);
        assertThat(filter2.getRadius(), is(4));

    }

    @Test
    public void shouldReadSpec_image_withMaxPixelsError_andArea() throws IOException {
        SpecImage spec = (SpecImage)readSpec("image: file imgs/image.png, error 112 px, area 10 10 100 20");
        assertThat(spec.getImagePaths(), contains("./imgs/image.png"));
        assertThat(spec.getErrorRate().getValue(), is(112.0));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PIXELS));
        assertThat(spec.getTolerance(), is(25));
        assertThat(spec.getSelectedArea(), is(new Rect(10,10,100,20)));
    }

    @Test
    public void shouldReadSpec_image_andBuildImagePath_withContextPath() throws IOException {
        SpecImage spec = (SpecImage) readSpec("image: file image.png", "some-component/specs");
        assertThat(spec.getImagePaths(), contains("some-component/specs/image.png"));
    }

    /**
     * Comes from https://github.com/galenframework/galen/issues/171
     * @throws IOException
     */
    @Test
    public void shouldReadSpec_image_toleranceAndErrorRate_fromConfig() throws IOException {
        System.setProperty("galen.spec.image.tolerance", "21");
        System.setProperty("galen.spec.image.error", "121%");
        SpecImage spec = (SpecImage)readSpec("image: file image.png");

        assertThat(spec.getTolerance(), is(21));
        assertThat(spec.getErrorRate().getValue(), is(121.0));
        assertThat(spec.getErrorRate().getType(), is(SpecImage.ErrorRateType.PERCENT));


        System.getProperties().remove("galen.spec.image.tolerance");
        System.getProperties().remove("galen.spec.image.error");
    }

    @Test
    public void shouldReadSpec_component() throws IOException {
        SpecComponent spec = (SpecComponent)readSpec("component: some.spec");
        assertThat(spec.isFrame(), is(false));
        assertThat(spec.getSpecPath(), is("./some.spec"));
        assertThat(spec.getOriginalText(), is("component: some.spec"));
    }

    @Test
    public void shouldReadSpec_component_frame() throws IOException {
        SpecComponent spec = (SpecComponent)readSpec("component frame: some.spec");
        assertThat(spec.isFrame(), is(true));
        assertThat(spec.getSpecPath(), is("./some.spec"));
        assertThat(spec.getOriginalText(), is("component frame: some.spec"));
    }


    @Test(expectedExceptions={SyntaxException.class}, expectedExceptionsMessageRegExp="Cannot parse number: \"\"")
    public void givesError_specImage_withIncorrectArea() throws IOException {
        readSpec("image: file /imgs/path, area 10 10");
    }

    @Test(expectedExceptions={SyntaxException.class}, expectedExceptionsMessageRegExp="Unknown color: orrrrraangeee")
    public void givesError_withUnknownColor() throws IOException {
        readSpec("color scheme: 40% orrrrraangeee");
    }

    @Test(expectedExceptions={SyntaxException.class}, expectedExceptionsMessageRegExp="Cannot use theses sides: top bottom") 
    public void givesError_withIncorrect_sides_for_spec_on() throws IOException {
        readSpec("on top bottom: object 10px top");
    }
    
    @Test(expectedExceptions={SyntaxException.class}, expectedExceptionsMessageRegExp="Too many sides. Should use only 2") 
    public void givesError_withIncorrect_too_many_sides_for_spec_on() throws IOException {
        readSpec("on top bottom right: object 10px top");
    }
    
    @Test(expectedExceptions={NullPointerException.class}, expectedExceptionsMessageRegExp="Spec text should not be null") 
    public void givesError_whenTextIsNull() throws IOException {
        readSpec(null);
    }
    
    @Test(expectedExceptions={SyntaxException.class}, expectedExceptionsMessageRegExp="Spec text should not be empty") 
    public void givesError_whenTextIsEmpty() throws IOException {
        readSpec(" ");
    }
    
    @Test(expectedExceptions={SyntaxException.class}, expectedExceptionsMessageRegExp="Incorrect error rate syntax: \" 23 to 123px\"") 
    public void givesError_withIncorrect_errorRate_inSpec_centered() throws IOException {
        readSpec("centered horizontally inside: object 23 to 123px");
    }


    @Test(expectedExceptions = {SyntaxException.class}, expectedExceptionsMessageRegExp = "There are no images defined")
    public void givesSyntaxException_imageWithoutFile() throws IOException {
        readSpec("image: stretch");
    }

    @Test(expectedExceptions = {SyntaxException.class}, expectedExceptionsMessageRegExp = "Unknown parameter: imgs/file.png")
    public void givesSyntaxException_imageWithoutFileParameterName() throws IOException {
        readSpec("image: imgs/file.png");
    }


    private Spec readSpec(String specText) throws IOException {
        return new SpecReader(EMPTY_PROPERTIES).read(specText);
    }

    private Spec readSpec(String specText, String contextPath) throws IOException {
        return new SpecReader(EMPTY_PROPERTIES).read(specText, contextPath);
    }
    
    private List<Side> sides(Side...sides) {
        return Arrays.asList(sides);
    }

}
